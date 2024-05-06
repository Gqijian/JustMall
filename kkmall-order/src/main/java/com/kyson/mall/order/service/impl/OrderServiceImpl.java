package com.kyson.mall.order.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.to.mq.OrderTo;
import com.kyson.common.to.mq.SecKillOrderTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.common.vo.MemberRespVo;
import com.kyson.mall.order.constant.OrderConstant;
import com.kyson.mall.order.dao.OrderDao;
import com.kyson.mall.order.entity.OrderEntity;
import com.kyson.mall.order.entity.OrderItemEntity;
import com.kyson.mall.order.entity.PaymentInfoEntity;
import com.kyson.mall.order.enume.OrderStatusEnum;
import com.kyson.mall.order.feign.CartFeignService;
import com.kyson.mall.order.feign.MemberFeignService;
import com.kyson.mall.order.feign.ProductFeignService;
import com.kyson.mall.order.feign.WmsFeignService;
import com.kyson.mall.order.interceptor.LoginUserIntercepor;
import com.kyson.mall.order.service.OrderItemService;
import com.kyson.mall.order.service.OrderService;
import com.kyson.mall.order.service.PaymentInfoService;
import com.kyson.mall.order.to.OrderCreateTo;
import com.kyson.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params)
    {

        MemberRespVo respVo = LoginUserIntercepor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().orderByDesc("id")
        );
        List<OrderEntity> orderSn = page.getRecords().stream().map(order -> {

            List<OrderItemEntity> itemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setItemEntities(itemEntities);
            return order;
        }).collect(Collectors.toList());

        page.setRecords(orderSn);
        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException
    {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        MemberRespVo memberRespVo = LoginUserIntercepor.loginUser.get();

        //得到原请求的数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有的收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);


        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都要共享之前的请求
            RequestContextHolder.setRequestAttributes(requestAttributes);
            /**
             * feign 在远程调用之前要构造请求，调用很多拦截器
             */
            //远程查询所有购物车选中的购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

            //要启动服务，不需要登录状态的话 可以线程不共享
            R hasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });

            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        }, executor);


        //用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //其他数据自动计算
        //TODO 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //给前端一个令牌
        confirmVo.setOrderToken(token);

        //服务器也放一份令牌
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        CompletableFuture.allOf(getAddressFuture, cartFuture).get();
        return confirmVo;
    }

    //@GlobalTransactional 不适合高并发场景
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo)
    {

        confirmVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //验证令牌
        MemberRespVo memberRespVo = LoginUserIntercepor.loginUser.get();
        String orderToken = vo.getOrderToken();

        //令牌对比和删除必须保证原子性 使用脚本
        //脚本返回的是 0（失败） 和 1（删除成功 也就意味着对比成功）
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        //原子删锁，验证令牌
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);

        if (execute == 0L) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            //删除成功 令牌验证成功
            //下单：创建订单 验令牌 验价格 锁库存
            OrderCreateTo order = createOrder();

            //验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (payAmount.subtract(payPrice).abs().doubleValue() < 0.01) {

                //验价成功
                //保存订单
                saveOrder(order);

                //锁定库存 有异常 事务回滚订单数据
                //订单号 订单项（skuId, skuName, num）

                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);

                //TODO 远程锁库存
                //库存成功了，但是网络原因 超时了，订单回滚 库存不回滚

                //为了保证高并发，不使用 seata，库存服务自己回滚
                R r = wmsFeignService.orderLockStock(lockVo);

                if (r.getCode() == 0) {
                    //锁定库存成功
                    responseVo.setOrder(order.getOrder());

                    //TODO 订单创建成功发送消息给 MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return responseVo;
                } else {
                    //throw new NoStockException()
                    //锁定失败
                    responseVo.setCode(3);
                    return responseVo;
                }

            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }

        /*
        String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());
        if(!StringUtils.isEmpty(orderToken) && orderToken.equals(redisToken)){
            //令牌验证通过
            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId());

        }else {
            //不通过
            return null;
        }

         */
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn)
    {

        OrderEntity order = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order;
    }

    @Override
    public void closeOrder(OrderEntity entity)
    {
        //查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());

        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            try {
                //发给MQ
                //TODO 保证消息一定会发出去 每一个发送的消息都做好日志记录 (给数据库保存每一个消息的详细信息)
                //TODO 定期扫描数据库将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other.#", orderTo);

            } catch (Exception e) {
                //TODO 出现问题以后，将没发送成功的消息重试发送
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn)
    {

        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));

        return order_sn;
    }

    @Override
    public PayVo getOrderPay(String orderSn)
    {

        PayVo payVo = new PayVo();
        OrderEntity order = this.getOrderByOrderSn(orderSn);

        //BigDecimal.ROUND_UP 向上取值
        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(orderSn);

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity itemEntity = order_sn.get(0);

        payVo.setSubject(itemEntity.getSkuName());
        payVo.setBody(itemEntity.getSkuAttrsVals());
        return payVo;
    }

    /**
     * 处理支付宝的结果
     *
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo)
    {

        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(paymentInfoEntity);

        //判断交易状态是否成功
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {

            //支付成功
            this.baseMapper.updateOrderStatus(vo.getOut_trade_no(), OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void createSecKillOrder(SecKillOrderTo secKillOrder)
    {
        //TODO 保存订单信息
        OrderEntity order = new OrderEntity();
        order.setOrderSn(secKillOrder.getOrderSn());
        order.setMemberId(secKillOrder.getMemberId());

        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = secKillOrder.getSeckillPrice().multiply(new BigDecimal(secKillOrder.getNum()));
        order.setPayAmount(multiply);

        this.save(order);
        //TODO 保存订单项信息
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderSn(secKillOrder.getOrderSn());
        itemEntity.setRealAmount(multiply);
        itemEntity.setSkuQuantity(secKillOrder.getNum());

        //TODO 获取当前 sku 详细信息进行设置
    }

    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order)
    {

        OrderEntity orderEntity = order.getOrder();
        List<OrderItemEntity> orderItems = order.getOrderItems();

        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        orderItemService.saveBatch(orderItems);

    }

    private OrderCreateTo createOrder()
    {


        OrderCreateTo createTo = new OrderCreateTo();

        //生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

        //创建所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        //验价 计算价格
        computePrice(orderEntity, itemEntities);
        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);
        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities)
    {

        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity itemEntity : itemEntities) {

            //订单总额 就是叠加每一项真实金额
            BigDecimal realAmount = itemEntity.getRealAmount();
            BigDecimal couponAmount = itemEntity.getCouponAmount();
            BigDecimal integrationAmount = itemEntity.getIntegrationAmount();
            BigDecimal promotionAmount = itemEntity.getPromotionAmount();

            gift.add(new BigDecimal(itemEntity.getGiftIntegration()));
            growth.add(new BigDecimal(itemEntity.getGiftGrowth()));
            promotion = promotion.add(promotionAmount);
            integration = integration.add(integrationAmount);
            coupon = coupon.add(couponAmount);
            total = total.add(realAmount);

        }

        //订单价格相关
        orderEntity.setTotalAmount(total);

        //设置应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);

        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        //未删除
        orderEntity.setDeleteStatus(0);
    }


    private OrderEntity buildOrder(String orderSn)
    {

        MemberRespVo respVo = LoginUserIntercepor.loginUser.get();
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();

        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);

        entity.setMemberId(respVo.getId());

        //获取收货地址信息 以及运费
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });

        //设置运费信息
        entity.setFreightAmount(fareResp.getFare());
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setConfirmStatus(7);

        return entity;
    }

    /**
     * 构建所有订单项数据
     *
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn)
    {

        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();

        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());

            return itemEntities;
        }
        return null;
    }

    /**
     * 构建每一个订单项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem)
    {

        OrderItemEntity itemEntity = new OrderItemEntity();

        //订单信息：订单号

        //商品 spu 信息
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId());
        itemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        itemEntity.setSpuName(spuInfoVo.getSpuName());
        itemEntity.setCategoryId(spuInfoVo.getCatalogId());

        //商品 sku 信息
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());

        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());

        //优惠信息

        //积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());

        //订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));

        BigDecimal origin = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity()));
        BigDecimal subtract = origin.subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        //当前订单项的实际金额 总额减去其他优惠
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

    @Transactional(timeout = 30)
    public void a()
    {

        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        b();
        c();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void b()
    {

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void c()
    {

    }

}