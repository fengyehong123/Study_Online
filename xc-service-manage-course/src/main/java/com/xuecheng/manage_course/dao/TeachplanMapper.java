package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper  // 使用mybaits的mapper接口进行查询
public interface TeachplanMapper {

    // 根据课程的id查询所有的课程节点
    public TeachplanNode selectList(String courseId);
}
