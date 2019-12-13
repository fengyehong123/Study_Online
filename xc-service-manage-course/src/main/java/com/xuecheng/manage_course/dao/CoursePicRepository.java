package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Administrator.
 */
public interface CoursePicRepository extends JpaRepository<CoursePic,String> {

    // 根据id删除课程和图片的关联,并获取返回值
    // 当返回值>0,表示删除成功的记录数
    long deleteByCourseid(String courseId);

}
