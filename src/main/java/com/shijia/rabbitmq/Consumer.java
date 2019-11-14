package com.shijia.rabbitmq;

import com.rabbitmq.client.*;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/***
 * 消费者
 * author Dingshijia
 * DATE  2019/11/14
 */
public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        //基于TCP物理连接（创建了一个物理连接）
        Connection conn = RabbitUtils.getConnection();
        //创建通道
        Channel channel = conn.createChannel();
        //为通道绑定队列
        channel.queueDeclare(RabbitConstant.QUEUE_HELLOWORLD, false, false, false, null);
        /**
         *  创建一个消息消费者
         第一个参数是队列名称
         第二个参数代表是否自动确认收到消息,设置为false代表手动编程来确认消息
         第三个参数要传入DefaultConsumer实现类
         */

        channel.basicConsume(RabbitConstant.QUEUE_HELLOWORLD, false, new Reciver(channel));
    }
}

//创建类
class Reciver extends DefaultConsumer {
    private Channel channel;

    //重写构造函数，Channnl通道对象需要从外层传入，在handleDelivery中要用到
    public Reciver(Channel channel) {
        super(channel);
        this.channel = channel;
    }

    //重写构造函数，消息是在这处理的
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//        super.handleDelivery(consumerTag, envelope, properties, body);
        //接收数据并签收数据
        //body为接收数据
        String messageBody = new String(body);
        //打印收到消息
        System.out.println("消费者接收到:" + messageBody);
        /**
         *   签收消息，确认消息
         第一个参数:envelope.getDeliveryTag()获取这个消息的TagId
         第二个参数：false只确认签收当前的消息，设置为true的时候则代表签收消费者所有未签收的消息
         */
        channel.basicAck(envelope.getDeliveryTag(), false);
    }
}
