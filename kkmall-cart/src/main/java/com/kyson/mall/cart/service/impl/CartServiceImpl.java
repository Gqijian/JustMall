package com.kyson.mall.cart.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.kyson.common.utils.R;
import com.kyson.mall.cart.feign.ProductFeignService;
import com.kyson.mall.cart.interceptor.CartInterceptor;
import com.kyson.mall.cart.service.CartService;
import com.kyson.mall.cart.to.UserInfoTo;
import com.kyson.mall.cart.vo.Cart;
import com.kyson.mall.cart.vo.CartItem;
import com.kyson.mall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service("cartService")
public class CartServiceImpl implements CartService {

    private final String CART_PREFIX = "mall:cart:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException
    {

        BoundHashOperations cartOps = getCartOps();


        String itemJson = (String) cartOps.get(skuId);
        if (StringUtils.isEmpty(itemJson)) {
            //购物车无此商品
            CartItem cartItem = new CartItem();

            //远程查询要添加的商品的信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {

                R data = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = data.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                //新商品添加购物车
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfo.getPrice());
            }, executor);

            //远程查询sku组合信息
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);


            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            //查询sku的组合信息
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        } else {
            //购物车有此商品 修改数量即可
            CartItem cartItem = JSON.parseObject(itemJson, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;

        }

    }

    @Override
    public CartItem getCartItem(Long skuId)
    {

        BoundHashOperations cartOps = getCartOps();
        String itemJson = (String) cartOps.get(skuId);
        CartItem cartItem = JSON.parseObject(itemJson, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException
    {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (!ObjectUtils.isEmpty(userInfoTo)) {
            //1、登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();

            //合并临时购物车的数据
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if(tempCartItems != null && tempCartItems.size() > 0){
                for (CartItem item: tempCartItems){
                    addToCart(item.getSkuId(), item.getCount());
                }

                //清除临时购物车
                clearCart(tempCartKey);
            }

            //合并完后 返回购物车数据
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        } else {

            //没登陆
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();

            //临时购物车
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);

        }

        return cart;
    }

    @Override
    public void clearCart(String cartKey)
    {

        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check)
    {

        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1?true:false);

        BoundHashOperations cartOps = getCartOps();
        cartOps.put(skuId, JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num)
    {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations cartOps = getCartOps();
        cartOps.put(skuId, JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId)
    {
        BoundHashOperations cartOps = getCartOps();
        cartOps.delete(skuId);
    }

    @Override
    public List<CartItem> getUserCartItems()
    {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if(userInfoTo.getUserId() == null){
            return null;
        }else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);

            //获取所有被选中的购物项
            List<CartItem> collect = cartItems.stream()
                    .filter(item -> item.getCheck())
                    .map(item->{
                        //更新为最新价格 不能用购物车价格
                        R price = productFeignService.getPrice(item.getSkuId());
                        String data = (String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                        return item;
                    })
                    .collect(Collectors.toList());
            return collect;
        }
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations getCartOps()
    {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        //商品添加购物车操作

        //简单操作
        //redisTemplate.opsForHash().get(cartKey, "1");

        //绑定 hash 操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }

    private List<CartItem> getCartItems(String cartKey){

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);

        List<Object> values = hashOps.values();
        if(values != null && values.size() > 0){
            List<CartItem> collect = values.stream().map((obj) -> {
                CartItem cartItem = JSON.parseObject(obj.toString(), CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());

            return collect;
        }
        return null;
    }
}
