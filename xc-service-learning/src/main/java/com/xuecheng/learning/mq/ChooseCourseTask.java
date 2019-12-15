package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.CourseLearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);
    @Autowired
    private CourseLearningService courseLearningService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 指定要监听的队列
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE)
    public void receiveChooseCourseTask(XcTask xcTask, Message message, Channel channel){

        // 获取到消息的内容
        String requestBody = xcTask.getRequestBody();
        // 存到数据库中的消息内容是json字符串,我们转换为map对象
        Map map = JSON.parseObject(requestBody, Map.class);

        // 解析出对象中的消息
        String userId = (String) map.get("userId");
        String courseId = (String) map.get("courseId");
        // 因为数据库中造的数据缺少了valid和开始结束时间,所以暂时用null
        // String valid = (String) map.get("valid");

        // 调用添加课程的方法,添加选课
        ResponseResult responseResult = courseLearningService.addCourse(userId, courseId, null, null, null, xcTask);

        if (responseResult.isSuccess()){
            // 如果添加选课成功,就向mq发送完成添加选课的消息
            // 指定交换机和路由key(注意:路由key要指定的是添加完成的key)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_ADDCHOOSECOURSE,RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY,xcTask);
        }
    }
}
