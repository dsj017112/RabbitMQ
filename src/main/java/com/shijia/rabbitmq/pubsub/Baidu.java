package com.shijia.rabbitmq.pubsub;

import com.rabbitmq.client.*;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;

/**
 * pubsub消费者
 * author Dingshijia
 * DATE  2019/11/14
 */
public class Baidu {
    public static void main(String[] args) throws IOException {
        Connection connection = RabbitUtils.getConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(RabbitConstant.QUEUE_BAIDU, false, false, false, null);
        //队列和交换机绑定
        //queueBind用于将队列与交换机绑定
        //参数一：队列名称
        //参数二：交换机名
        //参数三：路由key暂时用不到
        channel.queueBind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER, "");
        //每次直处理1条数据，处理一个获取一个
        channel.basicQos(1);
        //第二个参数为是否自动确认
        channel.basicConsume(RabbitConstant.QUEUE_BAIDU, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("百度收到气象信息：" + new String(body));
                //消息签收,第二个参数之签收当前这一条数据
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });

    }
}
