package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRepository extends JpaRepository<Teachplan,String>{

    // 根据课程id和父节点ID为进行查询 父节点只有为0的情况下才能查出一条,如果不是0的话,会查出很多条,我们用列表来接收
    public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
