# RabbitMQ
本仓库是RabbitMQ初学的Demo，其中包含了RabbitMQ 的常用工作模式，SpringBoot与RabbitMQ整合等。。。
RabbitMQ

公共类

    package com.shijia.rabbitmq.utils;
    
    import com.rabbitmq.client.Connection;
    import com.rabbitmq.client.ConnectionFactory;
    
    /**
     * 此类用于初始化连接类
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
    

    package com.shijia.rabbitmq.utils;
    
    /**
     * RabbitMQ常量类,避免多个地方使用同一个变量手写出错
     */
    public class RabbitConstant {
        public static final String QUEUE_HELLOWORLD = "helloworld";
        public static final String QUEUE_SMS = "sms";
        //exchange
        public static final String EXCHANGE_WEATHER = "weather";
        //queue
        public static final String QUEUE_BAIDU = "baidu";
        public static final String QUEUE_SINA = "sina";
    }
    



1.最原始版的"Hello,World!"

这种模式是最普通的模式一般在工作中很少使用，采用的方式为单个消费者，单个生产者。

    package com.shijia.rabbitmq;
    
    import com.rabbitmq.client.Channel;
    import com.rabbitmq.client.Connection;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    import java.util.concurrent.TimeoutException;
    
    /***
     * 生产者
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
    

    package com.shijia.rabbitmq;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    import java.util.concurrent.TimeoutException;
    
    /***
     * 消费者
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
    



2.work queues

这种模式，我这里是使用了12306抢票通知做的案例，系统处理完成订单之后要短信通知，系统处理的订单会放入RabbitMQ存储中，然后短信发送服务器去队列中获取，并且这里采用了channel.basicQos(1);代表处理完成几条再去获取，服务器资源好的处理的就多，这里使用了Thread.sleep(100);来做了一个服务器性能模拟。

这种模式为多对一，多个消费者，单个生产者

    package com.shijia.rabbitmq.workqueue;
    
    /***
     * 实体类
     */
    public class SMS {
        private String name;
        private String mobile;
        private String content;
    
        public SMS(String name, String mobile, String content) {
            this.name = name;
            this.mobile = mobile;
            this.content = content;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public String getMobile() {
            return mobile;
        }
    
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    
        public String getContent() {
            return content;
        }
    
        public void setContent(String content) {
            this.content = content;
        }
    }
    

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
    

    package com.shijia.rabbitmq.workqueue;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * 短信发送处理1
     */
    public class SMSSender1 {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_SMS, false, false, false, null);
            channel.basicQos(1);
            channel.basicConsume(RabbitConstant.QUEUE_SMS, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String jsonSMS = new String(body);
                    System.out.println("SMSSender1-短信发送成功:" + jsonSMS);
                    try {
                        //睡10毫秒
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //签收
                    channel.basicAck(envelope.getDeliveryTag(), false);
    
                }
            });
        }
    
    }
    

    package com.shijia.rabbitmq.workqueue;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * 短信发送处理2
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
    

    package com.shijia.rabbitmq.workqueue;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * 短信发送处理3
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
    



3.Publish/Subscribe发布订阅模式

发布订阅模式，基本算是在workqueues模式上做了一个升级

这里做了一个案例是气象局发布信息，然后订阅者可以收到发布者发出的信息

这里使用到了Exchange交换机

发布者不需要跟队列进行交互，是和交换机做了交互

然后订阅者创建队列来绑定交换机然后获取数据，最后签收



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
    

    package com.shijia.rabbitmq.pubsub;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * pubsub消费者
     */
    public class Baidu {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_BAIDU, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称
            //参数二：交换机名
            //参数三：路由key暂时用不到
            channel.queueBind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER, "");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_BAIDU, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("百度收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    

    package com.shijia.rabbitmq.pubsub;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * pubsub消费者
     */
    public class Sina {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_SINA, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称
            //参数二：交换机名
            //参数三：路由key暂时用不到
            channel.queueBind(RabbitConstant.QUEUE_SINA, RabbitConstant.EXCHANGE_WEATHER, "");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_SINA, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("新浪收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    



4.Routing路由模式

Routing路由模式是Sub/Pub发布订阅模式的升级

发布订阅模式是无条件的分配和接收

路由模式采用了路由Routing Key的方式根据条件筛选后发送给消费者队列

路由模式下交换机的类型被称为direct，所有创建交换机类型是应该为direct类型

可以把Routing理解为是发布订阅模式加上了一个数据筛选的功能

这里用的案例和上个一样，只是加了一个数据筛选，假如pub/sub发布的数据为全世界气象

但是现在只需要国内的气象信息，这样就用的了Routing路由模式

路由key在使用的时候如果重复了，默认获取最后一个Routing Key，队列每绑定一次交换机可根据一个Routing Key获取一条消息

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
    

    package com.shijia.rabbitmq.routing;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * Routing消费者
     */
    public class Baidu {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_BAIDU, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称是、
            //参数二：交换机名
            //参数三：路由key
            //一个队列可以绑定多个交换机，一个交换机也可以绑定多个队列，是双向的
            channel.queueBind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER_ROUTING, "china.shandong.jinan.20191114");
            channel.queueBind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER_ROUTING, "china.shandong.qingdao.20191114");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_BAIDU, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("百度收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    

    package com.shijia.rabbitmq.routing;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * Routing消费者
     */
    public class Sina {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_SINA, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称
            //参数二：交换机名
            //参数三：路由key
            //一个队列可以绑定多个交换机，一个交换机也可以绑定多个队列，是双向的
            channel.queueBind(RabbitConstant.QUEUE_SINA, RabbitConstant.EXCHANGE_WEATHER_ROUTING, "us.cal.la.20191114");
            channel.queueBind(RabbitConstant.QUEUE_SINA, RabbitConstant.EXCHANGE_WEATHER_ROUTING, "china.shandong.heze.20191114");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_SINA, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("新浪收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    



5.Topics主题模式

Topics主题模式是针对于Routing模式的升级

主题Topic模式提供了对RouteKey模糊匹配的功能，可以简化程序编写。

主题模式下，模糊匹配表达式规则为：

"*" 为匹配单个关键字

"#" 匹配所有关键字

主题模式下交换机的类型被称为topic

注意：Routing Key是不能重复的

    package com.shijia.rabbitmq.topic;
    
    import com.rabbitmq.client.Channel;
    import com.rabbitmq.client.Connection;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    import java.util.Iterator;
    import java.util.LinkedHashMap;
    import java.util.Map;
    import java.util.concurrent.TimeoutException;
    
    /**
     * Topic主题模式
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
            Connection connection = RabbitUtils.getConnection();
            Channel channel = connection.createChannel();
            Iterator<Map.Entry<String, String>> itr = area.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, String> me = itr.next();
                //第二个参数 Routing Key 相当于数据筛选的条件
                channel.basicPublish(RabbitConstant.EXCHANGE_WEATHER_TOPIC, me.getKey(), null, me.getValue().getBytes());
            }
            //关闭
            channel.close();
            connection.close();
        }
    }
    

    package com.shijia.rabbitmq.topic;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * topic消费者
     */
    public class Baidu {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_BAIDU, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称是、
            //参数二：交换机名
            //参数三：路由key
            //一个队列可以绑定多个交换机，一个交换机也可以绑定多个队列，是双向的
            channel.queueBind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER_TOPIC, "*.*.*.20191114"); //*.*.heze.*
            //如果不想使用这用匹配方式了,使用这句话就可以解绑了
            //channel.queueUnbind(RabbitConstant.QUEUE_BAIDU, RabbitConstant.EXCHANGE_WEATHER_TOPIC, "*.*.*.20191114");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_BAIDU, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("百度收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    

    package com.shijia.rabbitmq.topic;
    
    import com.rabbitmq.client.*;
    import com.shijia.rabbitmq.utils.RabbitConstant;
    import com.shijia.rabbitmq.utils.RabbitUtils;
    
    import java.io.IOException;
    
    /**
     * topic消费者
     */
    public class Sina {
        public static void main(String[] args) throws IOException {
            Connection connection = RabbitUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(RabbitConstant.QUEUE_SINA, false, false, false, null);
            //队列和交换机绑定
            //queueBind用于将队列与交换机绑定
            //参数一：队列名称
            //参数二：交换机名
            //参数三：路由key
            //一个队列可以绑定多个交换机，一个交换机也可以绑定多个队列，是双向的
            channel.queueBind(RabbitConstant.QUEUE_SINA, RabbitConstant.EXCHANGE_WEATHER_TOPIC, "us.#");
            //每次直处理1条数据，处理一个获取一个
            channel.basicQos(1);
            //第二个参数为是否自动确认
            channel.basicConsume(RabbitConstant.QUEUE_SINA, false, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    System.out.println("新浪收到气象信息：" + new String(body));
                    //消息签收,第二个参数之签收当前这一条数据
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            });
    
        }
    }
    

消息确认机制Confim与Return

Confirm代表生产者将消息送到Broker时产生的状态，后续会出现两种情况：

1.ack代表Broker已经将数据接收，这里可以把Broker看做邮局，消息看成信封。

2.nack代表Broker拒收消息。原因有很多种，队列已满，限流，IO异常。。。。

这里可以理解为我们要去邮局发送一封信，但是邮局不支持我们发送的地区，这时候的状态就是nack。

Return代表消息被Broker正常接收（ack）后，但Broker没有对应的队列进行投递时产生的状态，消息被退回给生产者。

这里可以理解为我们的信已经送到了邮局，但是我们在邮件上没有写地址，邮局无法邮件，所以要给我们退回。这时的状态就应该为Return。

注意：上面两种状态只代表生产者与Broker之前消息投递的情况，与消费者是否接收/确认消息无关。也就是说上面两种状态指示发件人和邮局之间的交互关系，和收件人不会产生任何关系。

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
    


