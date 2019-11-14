package com.shijia.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.shijia.rabbitmq.utils.RabbitConstant;
import com.shijia.rabbitmq.utils.RabbitUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/***
 * 生产者
 * author Dingshijia
 * DATE  2019/11/14
 */
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        //基于TCP物理连接（创建了一个物理连接）
        Connection conn = RabbitUtils.getConnection();
        //创建通信"通道"，相当于TCP中的虚拟连接，物理连接的开通成本是很高的所以选择使用虚拟连接
        Channel channl = conn.createChannel();
        //在通道中创建队列(声明并创建一个队列，如果队列已存在，则使用这个队列，如果没有则创建)
        /**
         * 第一个参数：队列名称ID
         第二个参数：是否持久化：false对应不持久化数据，MQ停掉数据就会丢失,如果设置为true的话当机试MQ会把数据存储在MQ的虚拟主机里不会丢失，线上环境持久化是一定要做的
         第三个参数：是否私有化，false则代表所有消费者都可以访问，true代表只有第一次拥有他的消费者才能使用，其他消费者无法访问
         第四个参数：用完之后是否自动删除，false代表连接停掉后不自动删除这个队列
         第五个参数：其他额外的参数，null
         */
        channl.queueDeclare(RabbitConstant.QUEUE_HELLOWORLD, false, false, false, null);
        //数据发送
        /**
         *  第一个参数：exchange 交换机，暂时用不到，在后面进行发布订阅是才会用到
         第二个参数：队列名称，要和上面创建的队列的名称保持一致
         第三个参数：额外的设置属性,设置为null
         第四个参数：要传递的消息字节数据getBytes()
         */
        String message = "helloworld";
        channl.basicPublish("", RabbitConstant.QUEUE_HELLOWORLD, null, message.getBytes());
        //最后关闭
        channl.close();
        conn.close();
        System.out.println("数据发送成功！");
    }
}
