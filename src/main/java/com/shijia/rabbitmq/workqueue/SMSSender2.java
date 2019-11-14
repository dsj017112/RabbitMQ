package com.shijia.rabbitmq.workqueue;

import com.rabbitmq.client.*;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;

/**
 * 短信发送处理2
 * author Dingshijia
 * DATE  2019/11/14
 */
public class SMSSender2 {
    public static void main(String[] args) throws IOException {
        Connection connection = RabbitUtils.getConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(RabbitConstant.QUEUE_SMS, false, false, false, null);
        channel.basicQos(1);
        channel.basicConsume(RabbitConstant.QUEUE_SMS, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String jsonSMS = new String(body);
                System.out.println("SMSSender2-短信发送成功:" + jsonSMS);
                try {
                    //睡10毫秒
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //签收
                channel.basicAck(envelope.getDeliveryTag(), false);

            }
        });
    }

}
