package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {

    @Autowired
    private CourseService courseService;

    // 根据课程ID查询课程节点的详细信息
    @PreAuthorize("hasAnyAuthority('course_teachplan_list')")  // 当用户拥有course_teachplan_list权限时,才可访问此方法
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findteachplanList(courseId);
    }

    // 添加课程
    @PreAuthorize("hasAnyAuthority('course_teachplan_add')")
    @Override
    @PostMapping("/teachplan/add")  // 将json数据,解析为java的实体类对象
    public ResponseResult addTeachPlan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachPlan(teachplan);
    }

    // 查询我的课程
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page, @PathVariable("size") int size, CourseListRequest courseListRequest) {

        // 调用工具类,获取jwt的信息
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);

        // 根据公司的id返回查询该公司下面的所有的课程,其他公司的课程不能被查到
        String company_id = userJwt.getCompanyId();
        QueryResponseResult<CourseInfo> courseList = courseService.findCourseList(company_id, page, size, courseListRequest);

        return courseList;
    }

    // 添加基础课程
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

    // 查询课程的基本信息
    @Override
    @GetMapping("/coursebase/findById/{courseId}")
    public CourseBase getCourseBaseById(@PathVariable("courseId") String courseId) throws RuntimeException {
        return courseService.getCourseBaseById(courseId);
    }

    // 修改课程的基本信息
    @Override
    @PostMapping("/coursebase/updateCoursebase/{courseId}")
    public ResponseResult updateCourseBase(@PathVariable("courseId") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBaseById(id,courseBase);
    }

    // 根据id查询课程营销的信息
    @Override
    @GetMapping("/coursebase/findMarketFormById/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

    // 根据id更新课程营销的信息
    @Override
    @PostMapping("/coursebase/editMarketForm/{courseId}")
    public ResponseResult updateCourseMarket(@PathVariable("courseId") String id, @RequestBody CourseMarket courseMarket) {
        return courseService.updateCourseMarket(id,courseMarket);
    }

    // 保存上传成功的图片和课程的关联关系
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic") String pic) {
        return courseService.saveCoursePic(courseId,pic);
    }

    // 根据id查询课程关联的图片照片
    @PreAuthorize("hasAnyAuthority('course_pic_list')")
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    // 删除课程的图片
    @Override
    @DeleteMapping("/coursepic/delete")  // 删除图片的id是通过?key=value的形式删除的
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    // 课程的视图查询
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.courseview(id);
    }

    // 预览课程
    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);
    }

    // 一键发布接口
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }

    // 保存课程计划与媒资文件的关联
    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveCourseWithMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveCourseWithMedia(teachplanMedia);
    }
}
