package com.xuecheng.test.rabbitmq;
import com.rabbitmq.client.Channel;
import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ReceiveHandler {

    // 监听email队列
    @RabbitListener(queues = {
            // 监听多个队列
            RabbitmqConfig.QUEUE_INFORM_EMAIL,
            RabbitmqConfig.QUEUE_INFORM_SMS
    })
    public void receive_email(String msg,Message message,Channel channel){
        System.out.println(msg);
    }

}