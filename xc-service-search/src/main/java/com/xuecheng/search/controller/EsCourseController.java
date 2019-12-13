package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {

    @Autowired
    private EsCourseService esCourseService;

    // 前端的静态门户页面的课程搜索,可以进行分页和条件搜素
    @Override
    @GetMapping(value="/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page,@PathVariable("size") int size,CourseSearchParam courseSearchParam) throws IOException {

        return esCourseService.list(page,size,courseSearchParam);
    }

    // 根据id查询课程信息
    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getAll(@PathVariable("id") String courseId) {
        return esCourseService.getAll(courseId);
    }

    // 根据课程计划id查询课程媒资信息
    @Override
    @GetMapping(value="/getmedia/{teachplanId}")
    public TeachplanMediaPub getmedia(@PathVariable("teachplanId") String teachplanId) {
        // 将一个id加入数组,传给service方法
        String[] teachplanIds = {teachplanId};
        QueryResponseResult<TeachplanMediaPub> result = esCourseService.getmedia(teachplanIds);

        QueryResult<TeachplanMediaPub> queryResult = result.getQueryResult();
        if (queryResult != null){
            List<TeachplanMediaPub> queryResultList = queryResult.getList();
            if (queryResultList != null && queryResultList.size() > 0){
                // 如果只传入一个id的话,那么肯定是第一个下标
                return queryResultList.get(0);
            }
        }

        return new TeachplanMediaPub();
    }
}
