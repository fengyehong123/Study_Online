package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// 入门程序的消费者
public class Consumer01 {

    // 创建一个队列的名称
    private static final String QUEUE = "helloWord";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 通过连接工厂创建新的连接和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.118.130");
        connectionFactory.setPort(5672);  // 通信端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        // 设置虚拟机,一个mq的服务可以设置多个虚拟机,每个虚拟机就相当于一个独立的mq
        connectionFactory.setVirtualHost("/");

        // 建立新的连接
        Connection connection = connectionFactory.newConnection();
        // 创建会话通道,生产者和mp所有的服务都在channel 通道中完成
        Channel channel = connection.createChannel();

        // 消费者声明要监听的队列(生产者和消费者监听的队列要相同,两者都要监听队列)
        channel.queueDeclare(QUEUE,true,false,false,null);

        // 消费方法,实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel){

            // 当消费者接收到消息之后,次方法将被调用

            /**
             *
             * @param consumerTag  消费者标签,用来标识消费者的,在监听队列的时候设置(可以不设置)  channel.basicConsume
             * @param envelope 信封,可以通过
             * @param properties  可以获取消息的属性(如果生产者设置的话)
             * @param body  消息的内容
             * @throws IOException
             */
            // 当消费者监听的队列监听到方法的时候,会调用此方法
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // 获取交换机
                String exchange = envelope.getExchange();
                // 消息id,mq在channel中用来标识消息的id,可用于确认消息已经接收
                long deliveryTag = envelope.getDeliveryTag();
                // 获取消息的内容 把字节数组的消息转换为字符串
                String message = new String(body, "utf-8");
                System.out.println("receive message:" + message);

            }
        };


        // 消费者开始监听队列
        /*
        *  参数:
        *  String queue, boolean autoAck, Consumer callback
        *
        *  参数1: queue 队列名称
        *  参数2: autoAck 自动回复,当消费者接收到消息后要告诉mq消息已经接收,如果将此参数设置为true表示会自动回复mq.如果设置为false,则要通过编程实现
        *  参数3: callback 消费方法,当消费者接收到消息要执行的方法
        * */
        channel.basicConsume(QUEUE,true, defaultConsumer);

        // 消费者要一直监听队列,因此不能关闭连接



    }
}
