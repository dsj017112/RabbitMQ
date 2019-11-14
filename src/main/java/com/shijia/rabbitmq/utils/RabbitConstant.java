package com.shijia.rabbitmq.utils;

/**
 * RabbitMQ常量类,避免多个地方使用同一个变量手写出错
 * author Dingshijia
 * DATE  2019/11/14
 */
public class RabbitConstant {
    public static final String QUEUE_HELLOWORLD = "helloworld";
    public static final String QUEUE_SMS = "sms";
    //exchange
    public static final String EXCHANGE_WEATHER = "weather";
    public static final String EXCHANGE_WEATHER_ROUTING = "weather_routing";
    public static final String EXCHANGE_WEATHER_TOPIC = "weather_topic";
    //queue
    public static final String QUEUE_BAIDU = "baidu";
    public static final String QUEUE_SINA = "sina";
}
