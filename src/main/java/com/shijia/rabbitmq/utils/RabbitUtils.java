package com.shijia.rabbitmq.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 此类用于初始化连接类
 * author Dingshijia
 * DATE  2019/11/14
 */
public class RabbitUtils {
    //connectionFactory用于创建MQ的物理连接
    private static ConnectionFactory connectionFactory = new ConnectionFactory();

    static {
        //服务器的ip地址
        connectionFactory.setHost("127.0.0.1");
        //5672是RabbitMQ的默认端口
        connectionFactory.setPort(5672);
        //用户名
        connectionFactory.setUsername("guest");
        //密码
        connectionFactory.setPassword("guest");
        //虚拟主机
        connectionFactory.setVirtualHost("/test");
    }

    //创建连接
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = connectionFactory.newConnection();
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
