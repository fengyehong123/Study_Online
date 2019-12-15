package com.xuecheng.order.mq;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// @Component 不添加到spring的容器汇中
public class ChooseCourseTaskTest {

    // 添加日志对象
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTaskTest.class);

    // @Scheduled(fixedRate = 3000)  // 上次执行开始时间后3秒执行
    // @Scheduled(fixedDelay = 5000) 上次执行完毕后5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000)  第一次延迟3秒，以后每隔5秒执行一次
    // @Scheduled(cron="0/3 * * * * *")  // 每隔3秒执行一次,定义任务的策略
    public void task1(){
        LOGGER.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务1结束===============");
    }

    @Scheduled(fixedRate = 3000)  // 上次执行开始时间后3秒执行
    // @Scheduled(cron="0/3 * * * * *")  // 每隔3秒执行一次,定义任务的策略
    public void task2(){
        LOGGER.info("===============测试定时任务2开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("===============测试定时任务2结束===============");
    }
}
