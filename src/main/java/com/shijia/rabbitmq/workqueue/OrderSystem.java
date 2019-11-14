package com.shijia.rabbitmq.workqueue;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/***
 * workqueue生产者
 * author Dingshijia
 * DATE  2019/11/14
 */
public class OrderSystem {
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建一个连接
        Connection connection = RabbitUtils.getConnection();
        //创建一个信道
        Channel channel = connection.createChannel();
        //创建一个队列
        channel.queueDeclare(RabbitConstant.QUEUE_SMS, false, false, false, null);
        for (int i = 0; i < 200; i++) {
            SMS sms = new SMS("乘客" + i, "139000000" + i, "您的车票已预订成功！");
            //进行序列号
            String jsonSMS = new Gson().toJson(sms);
            //发布命令
            channel.basicPublish("", RabbitConstant.QUEUE_SMS, null, jsonSMS.getBytes());
        }
        System.out.println("发送数据成功！");
        //关闭连接
        channel.close();
        connection.close();
    }
}
