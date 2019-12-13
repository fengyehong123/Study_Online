package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.service.PageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// 监听MQ,接收页面发布的消息
@Component
public class ConsumerPostPage {
    // 日志的记录对象
    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);

    @Autowired
    private PageService pageService;

    // 从配置文件中获取监听的队列的名称
    @RabbitListener(queues = {"${xuecheng.mq.queue1}"})
    public void postPage(String msg){
        // 解析消息 把字符串json转换为map
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");

        // 校验页面是否合法
        CmsPage page = pageService.findCmsPageById(pageId);
        if (page == null){
            // 将错误记录到日志中
            LOGGER.error("receive postPage msg, cmsPage is null,pageId:{}",pageId);
            return;
        }

        // 调用service方法将页面从Grids中下载到服务器
        pageService.savePageToServerPath(pageId);
    }

    // 从配置文件中获取监听的队列的名称
    @RabbitListener(queues = {"${xuecheng.mq.queue2}"})
    public void postPage1(String msg){
        // 解析消息 把字符串json转换为map
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String) map.get("pageId");
        System.out.println("");
        // 校验页面是否合法
        CmsPage page = pageService.findCmsPageById(pageId);
        if (page == null){
            // 将错误记录到日志中
            LOGGER.error("receive postPage msg, cmsPage is null,pageId:{}",pageId);
            return;
        }

        // 调用service方法将页面从Grids中下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
