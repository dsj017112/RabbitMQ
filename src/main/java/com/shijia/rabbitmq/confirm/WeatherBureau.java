package com.shijia.rabbitmq.confirm;

import com.rabbitmq.client.*;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 消息确认机制Confim与Return
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
        area.put("us.cal.la.20191115", "美国加州20191114天气数据");
        area.put("se.hju.ol.20191212", "东京州20191114天气数据");
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();
        //开启confirm监听模式
        channel.confirmSelect();
        //监听到之后会回调
        channel.addConfirmListener(new ConfirmListener() {
            //第二个参数代表接收的数据是否为批量接收，一般我们用不到
            public void handleAck(long l, boolean b) throws IOException {
                System.out.println("消息已被Broke接收，Tag:" + l);
            }

            public void handleNack(long l, boolean b) throws IOException {
                System.out.println("消息已被Broke拒收，Tag:" + l);
            }
        });
        //这里是当发送的消息没有找到对应的消费者的时候，并且被退回（Renturn）的情况下触发
        //是否被退回取决于利用通道发布消息时的mandatory参数，为true时表示退回，为false时则直接将消息放弃
        channel.addReturnListener(new ReturnCallback() {
            public void handle(Return r) {
                System.err.println("=================");
                System.err.println("Return编码：" + r.getReplyCode() + "-Return描述:" + r.getReplyText());
                System.err.println("交换机：" + r.getExchange() + "-路由Key:" + r.getRoutingKey());
                System.err.println("消息主题：" + new String(r.getBody()));
                System.err.println("=================");
            }
        });
        Iterator<Map.Entry<String, String>> itr = area.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> me = itr.next();
            //第二个参数 Routing Key 相当于数据筛选的条件
            //第三个参数 mandatory true代表如果消息无法正常投递则return回生产者，如果为false则直接将消息放弃。
            channel.basicPublish(RabbitConstant.EXCHANGE_WEATHER_TOPIC, me.getKey(), true, null, me.getValue().getBytes());
        }
        //关闭
      /*  channel.close();
        connection.close();*/
    }
}
