package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;
    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 查询前n条任务
    public List<XcTask> findXcTaskList(Date updateTime,int size){

        // 设置分页参数 查询第一页的size条记录
        Pageable pageable = PageRequest.of(0, size);
        // 分页查询
        Page<XcTask> list = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        // 根据分页查询对象获取List<XcTask>对象
        List<XcTask> content = list.getContent();
        return content;
    }

    // 发布消息 指定任务,交换机,路由key
    public void publish(XcTask xcTask,String ex,String routingKey){
        // 判断要发送的任务是否存在
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(xcTask.getId());
        if (optionalXcTask.isPresent()){
            rabbitTemplate.convertAndSend(ex,routingKey,xcTask);
            XcTask task = optionalXcTask.get();
            // 更新为最新的时间
            task.setUpdateTime(new Date());
            // 保存最新的对象
            xcTaskRepository.save(task);
        }
    }

    // 获取任务 传入id和任务的版本号
    @Transactional
    public int getTask(String id,int version){
        // 通过乐观锁的方式来更新数据库表,如果结果>0,说明任务获取成功
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }

    // 完成任务,当接收到学习微服务通过MQ发送的消息之后,就会把Xc_task数据库中指定的任务删除,然后添加到历史数据库中
    @Transactional
    public void finishTask(String taskId){
        // 先根据id查询任务是否存在
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(taskId);
        if (optionalXcTask.isPresent()){
            XcTask xcTask = optionalXcTask.get();
            xcTask.setDeleteTime(new Date());
            // 把XcTask对象拷贝一份给历史对象
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);

            // 保存到数据库
            xcTaskHisRepository.save(xcTaskHis);
            // 删除任务
            xcTaskRepository.delete(xcTask);
        }
    }

}
