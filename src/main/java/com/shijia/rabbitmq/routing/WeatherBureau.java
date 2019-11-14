package com.shijia.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Routing路由模式
 * author Dingshijia
 * DATE  2019/11/14
 */
public class WeatherBureau {
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建一个Map
        Map area = new LinkedHashMap<String, String>();
        area.put("china.shandong.jinan.20191114", "中国山东济南20191114天气数据");
        area.put("china.shandong.qingdao.20191114", "中国山东青岛20191114天气数据");
        area.put("china.shandong.heze.20191114", "中国山东菏泽20191114天气数据");
        area.put("us.cal.la.20191114", "美国洛杉矶20191114天气数据");
        area.put("us.cal.la.20191114", "美国加州20191114天气数据");
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();
        Iterator<Map.Entry<String, String>> itr = area.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> me = itr.next();
            //第二个参数 Routing Key 相当于数据筛选的条件
            channel.basicPublish(RabbitConstant.EXCHANGE_WEATHER_ROUTING, me.getKey(), null, me.getValue().getBytes());
        }
        //关闭
        channel.close();
        connection.close();
    }
}
