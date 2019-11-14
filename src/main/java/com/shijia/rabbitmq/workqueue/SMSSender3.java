package com.shijia.rabbitmq.workqueue;

import com.rabbitmq.client.*;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;

/**
 * 短信发送处理3
 * author Dingshijia
 * DATE  2019/11/14
 */
public class SMSSender3 {
    public static void main(String[] args) throws IOException {
        Connection connection = RabbitUtils.getConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(RabbitConstant.QUEUE_SMS, false, false, false, null);
        //如果不写basicQos则自动MQ会将所有请求平均发送（分配）给所有消费者
        //basicQos, MQ不在对消费着一次发送多个请求，而是消费着处理完一个消息后（确认后），再从队列中获取一个新的
        //参数为处理几个，完成之后在进行获取
        channel.basicQos(1);//处理一个获取一个
        channel.basicConsume(RabbitConstant.QUEUE_SMS, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String jsonSMS = new String(body);
                System.out.println("SMSSender3-短信发送成功:" + jsonSMS);
                try {
                    //睡10毫秒
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //签收
                channel.basicAck(envelope.getDeliveryTag(), false);

            }
        });
    }

}
