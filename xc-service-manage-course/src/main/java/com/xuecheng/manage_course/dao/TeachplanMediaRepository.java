package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String>{

    // 根据课程id查询列表(是课程id查询,并不是主键查询)
    List<TeachplanMedia> findByCourseId(String courseId);
}
