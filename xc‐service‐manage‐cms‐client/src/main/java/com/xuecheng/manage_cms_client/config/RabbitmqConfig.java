package com.xuecheng.manage_cms_client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    //队列bean的名称
    public static final String QUEUE_CMS_POSTPAGE_01 = "queue_cms_postpage_01";
    //队列bean的名称
    public static final String QUEUE_CMS_POSTPAGE_02 = "queue_cms_postpage_02";
    //交换机的名称
    public static final String EX_ROUTING_CMS_POSTPAGE="ex_routing_cms_postpage";

    //队列的名称 从配置文件中获取配置
    @Value("${xuecheng.mq.queue1}")
    public String queue_cms_postpage_name1;
    @Value("${xuecheng.mq.queue2}")
    public String queue_cms_postpage_name2;
    //routingKey 即站点Id
    @Value("${xuecheng.mq.routingKey1}")
    public String routingKey1;
    @Value("${xuecheng.mq.routingKey2}")
    public String routingKey2;

    // 交换机配置使用direct类型
    @Bean(EX_ROUTING_CMS_POSTPAGE)
    public Exchange EXCHANGE_TOPICS_INFORM() {
        return ExchangeBuilder.directExchange(EX_ROUTING_CMS_POSTPAGE).durable(true).build();
    }

    //声明队列
    @Bean(QUEUE_CMS_POSTPAGE_01)
    public Queue QUEUE_CMS_POSTPAGE() {
        Queue queue = new Queue(queue_cms_postpage_name1);
        return queue;
    }
    //声明队列
    @Bean(QUEUE_CMS_POSTPAGE_02)
    public Queue QUEUE_CMS_POSTPAGE1() {
        Queue queue = new Queue(queue_cms_postpage_name2);
        return queue;
    }

    // 绑定交换机到队列
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS(@Qualifier(QUEUE_CMS_POSTPAGE_01) Queue queue,
                                            @Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey1).noargs();
    }
    // 绑定交换机到队列
    @Bean
    public Binding BINDING_QUEUE_INFORM_SMS1(@Qualifier(QUEUE_CMS_POSTPAGE_02) Queue queue,
                                            @Qualifier(EX_ROUTING_CMS_POSTPAGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey2).noargs();
    }

}
