package com.kyson.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.mall.order.dao.OrderItemDao;
import com.kyson.mall.order.entity.OrderEntity;
import com.kyson.mall.order.entity.OrderItemEntity;
import com.kyson.mall.order.entity.OrderReturnReasonEntity;
import com.kyson.mall.order.service.OrderItemService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@Service("orderItemService")
@RabbitListener(queues = {"hello-java-queue"})
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 声明监听的所有队列
     *
     * 参数可以写类型
     * 1、原生消息类型 Message message 头 + 体
     * 2、T <发送的消息的类型> 直接写消息类型的对象 也能直接转化
     * 3.Channel channel 一个客户端只有一条链接，所有数据都在通道里
     *
     * Queue 可以有很多人监听，只要受到消息，队列就删除，而且只能有一个收到此消息
     */
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity entity,
                               Channel channel) throws IOException
    {

        byte[] body = message.getBody();
        MessageProperties messageProperties = message.getMessageProperties();

        System.out.println("接收到的消息 " + message + " 内容 " + entity);

        //channel 内按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        //签收消息 非批量签收
        channel.basicAck(deliveryTag, false);

        //拒绝签收消息
        /**
         * basicNack(long deliveryTag, boolean multiple, boolean requeue)
         * basicReject(long deliveryTag, boolean requeue)
         *
         * requeue = false 丢弃 true 发回服务器，重新入队
         */
        channel.basicNack(deliveryTag, false, false);
        //channel.basicReject();

    }

    @RabbitHandler
    public void receiveMessage2(OrderEntity entity) {
        System.out.println("接收到的消息 " + " 内容 " + entity);
    }
}

