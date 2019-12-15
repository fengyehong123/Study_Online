package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {

    // 添加日志对象
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);
    @Autowired
    private TaskService taskService;

    // 定时发送添加选课任务
    // 每隔1分钟扫描消息表，向mq发送消息
    @Scheduled(cron = "0/3 * * * * *")
    public void sendChooseCourseTask(){
        // 取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        // 把当前的时间设置进去
        calendar.setTime(new Date());

        calendar.add(GregorianCalendar.MINUTE,-1);
        // 获取一分钟之前的时间
        Date time = calendar.getTime();
        // 100 代表要查看前100条任务
        List<XcTask> taskList = taskService.findXcTaskList(time, 100);

        // 调用service发布消息,将添加选课的任务发送给MQ
        for (XcTask xcTask : taskList) {
            // 获取任务,如果结果>0,则说明任务获取成功
            int count = taskService.getTask(xcTask.getId(), xcTask.getVersion());
            // 如果>0,则开始任务
            if (count>0){
                // 获取要交换机的名称
                String mqExchange = xcTask.getMqExchange();
                // 获取路由key
                String mqRoutingkey = xcTask.getMqRoutingkey();
                // 发布消息,把消息发送到MQ
                taskService.publish(xcTask,mqExchange,mqRoutingkey);
            }

        }
        System.out.println(taskList);
    }

    //监听队列
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChooseCourseTask(XcTask xcTask){

        if (xcTask!=null && StringUtils.isNotEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }
    }
}
