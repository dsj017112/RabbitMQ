package com.shijia.rabbitmq.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * pubsub发布订阅模式
 * 使用Exchange交换机来实现
 * author Dingshijia
 * DATE  2019/11/14
 */
public class WeatherBureau {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = RabbitUtils.getConnection();
        //输入文字
        String input = new Scanner(System.in).next();
        Channel channel = connection.createChannel();
        //这里因为没有和队列直接做交互，所以直接为空就可以
        channel.basicPublish(RabbitConstant.EXCHANGE_WEATHER, "", null, input.getBytes());
        //关闭
        channel.close();
        connection.close();
    }
}
