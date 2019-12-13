package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer01 {

    // 创建一个队列的名称
    private static final String QUEUE = "helloWord";

    public static void main(String[] args) {
        // 通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.130");
        connectionFactory.setPort(5672);  // 通信端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        // 设置虚拟机,一个mq的服务可以设置多个虚拟机,每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");

        Connection connection = null;
        Channel channel = null;
        try {
            // 建立新的连接
            connection = connectionFactory.newConnection();
            // 创建会话通道,生产者和mp所有的服务都在channel 通道中完成
            channel = connection.createChannel();
            // 声明一个队列,如果队列在mq中没有,则创建该队列
            // 声明队列的参数
            // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /**
             * 参数1: queue 队列名称
             * 参数2: durable 是否持久化,如果持久化,mq重启后,队列还在
             * 参数3: exclusive 是否独占连接,队列只允许在该连接中访问,如果 connection连接关闭,则队列自动删除,如果将此参数设置为true,可用于临时队列的创建
             * 参数4: autoDelete 自动删除,队列不再使用时是否自动删除队列,如果将此参数和exclusive参数设置为true,就可以实现临时队列(队列不用了就自动删除)
             * 参数5: 额外参数,可以设置一个队列的扩展参数,比如可以设置存活时间
             */
            channel.queueDeclare(QUEUE,true,false,false,null);

            // 发送消息
            /**
             * String exchange, String routingKey, BasicProperties props, byte[] body
             * 参数1: exchange 交换机,如果不指定,将使用mp的默认交换机(需要设置为 "" 空字符串)
             * 参数2: routingKey 路由key 交换机根据路由key来将消息转发到指定的队列,如果使用默认的交换机,routingKey设置为队列的名称
             * 参数3: props 消息的属性 实际中一般不用,一般会把相关消息放到消息体重
             * 参数4: body 消息的内容
             */
            // 定义一个消息的内容
            String message = "hello mw 黑马程序员";
            channel.basicPublish("",QUEUE ,null ,message.getBytes());

            System.out.println("send to mq" + message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 先关闭通道,然后关闭连接
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
