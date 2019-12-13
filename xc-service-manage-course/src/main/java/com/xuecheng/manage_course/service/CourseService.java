package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private CourseInfoMapper courseInfoMapper;
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    private CmsPageClient cmsPageClient;  // 要远程调用的接口
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    // 课程计划的查询
    public TeachplanNode findteachplanList(String courseId){
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    // 添加课程
    @Transactional
    public ResponseResult addTeachPlan(Teachplan teachplan) {
        // 重点就是要处理parentID
        if (teachplan == null
                || StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())
            ){
            // 如果上述信息有为空的话,抛出异常
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }

        // 获取课程ID
        String courseId = teachplan.getCourseid();
        // 获取parentId
        String parentId = teachplan.getParentid();

        // 如果添加课程的时候没有添加父节点的话
        if (StringUtils.isEmpty(parentId)){
            // 取出该课程的根节点
            parentId = this.getTeachplanRootId(courseId);
        }

        // 根据跟节点的id查询根节点对象
        Optional<Teachplan> optional = teachplanRepository.findById(parentId);
        Teachplan teachplan1 = optional.get();
        // 获取父节点的级别
        String parentGrade = teachplan1.getGrade();

        // 新节点
        Teachplan teachplanNew = new Teachplan();

        // 将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan, teachplanNew);
        // 给节点对象补全信息
        teachplanNew.setParentid(parentId);
        teachplan.setCourseid(courseId);
        // 我们添加节点的级别 不是2 就是3
        if (parentGrade.equals("1")){
            teachplanNew.setGrade("2");
        } else {
            teachplanNew.setGrade("3");
        }

        // 把新节点保存到数据库
        teachplanRepository.save(teachplanNew);

        // 告诉前端处理成功
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 查询课程的根节点,如果查询不到就要自动添加根节点
    private String getTeachplanRootId(String courseId){

        Optional<CourseBase> optionalBase = courseBaseRepository.findById(courseId);
        if (!optionalBase.isPresent()){
            return null;
        }
        // 获取到课程的信息
        CourseBase courseBase = optionalBase.get();

        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0){
            // 如果查询不到,就要自动添加根节点
            Teachplan teachplan = new Teachplan();
            // 因为是根节点,所以parentId为0
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            // 设置课程id
            teachplan.setCourseid(courseId);
            // 设置课程状态 0 代表未发布
            teachplan.setStatus("0");

            // 把根节点的课程保存到数据库
            teachplanRepository.save(teachplan);

            // 返回新自动添加的节点的id
            return teachplan.getId();
        }

        // 返回根节点的id
        return teachplanList.get(0).getId();
    }

    // 查询我的课程
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        if (page <= 0){
            page = 1;
        }
        if (size <=0){
            size = 20;
        }
        // 分页助手
        PageHelper.startPage(page,size);

        Page<CourseInfo> pageInfo = courseInfoMapper.findCourseList(courseListRequest);

        // 如果查询不到信息,说明查询条件有误
        if (pageInfo == null){
            // 非法参数
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }

        long total = pageInfo.getTotal();
        List<CourseInfo> result = pageInfo.getResult();

        QueryResult queryResult = new QueryResult();
        queryResult.setList(result);
        queryResult.setTotal(total);

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }

    // 添加根课程要修改数据库,添加事务
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {

        //课程状态默认为未发布
        courseBase.setStatus("202001");
        CourseBase base = courseBaseRepository.save(courseBase);
        String id = base.getId();

        AddCourseResult addCourseResult = new AddCourseResult(CommonCode.SUCCESS,id);

        return addCourseResult;
    }

    // 查询课程的基本信息
    public CourseBase getCourseBaseById(String courseId) {

        if (StringUtils.isEmpty(courseId)){
            // 课程id为空
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

        Optional<CourseBase> optionalBase = courseBaseRepository.findById(courseId);
        if (!optionalBase.isPresent()){
            // 如果根据id查询不到数据的话,说明课程id出错
            ExceptionCast.cast(CommonCode.FAIL);
        }

        CourseBase courseBase = optionalBase.get();

        return courseBase;


    }

    // 修改课程的基本信息
    @Transactional
    public ResponseResult updateCourseBaseById(String id, CourseBase courseBase) {

        if (StringUtils.isEmpty(id)){
            // 课程id为空
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (!optionalCourseBase.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        CourseBase base = optionalCourseBase.get();
        // BeanUtils.copyProperties(courseBase, base);
        base.setName(courseBase.getName());
        base.setMt(courseBase.getMt());
        base.setSt(courseBase.getSt());
        base.setGrade(courseBase.getGrade());
        base.setStudymodel(courseBase.getStudymodel());
        base.setUsers(courseBase.getUsers());
        base.setDescription(courseBase.getDescription());

        CourseBase save = courseBaseRepository.save(base);
        if (save.getId() != null){
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }

    // 根据id查询课程营销信息
    public CourseMarket getCourseMarketById(String courseId) {

        if (StringUtils.isEmpty(courseId)){
            // 课程id为空
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }

        Optional<CourseMarket> optionalMarket = courseMarketRepository.findById(courseId);
        if (optionalMarket.isPresent()){
            CourseMarket courseMarket = optionalMarket.get();
            return courseMarket;
        }

        return null;
    }

    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        if (StringUtils.isEmpty(id)){
            // 课程id为空
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CourseMarket market = this.getCourseMarketById(id);

        if (market == null){
            // 插入
            CourseMarket one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one);
            courseMarketRepository.save(one);
        } else {
            // 更新
            market.setCharge(courseMarket.getCharge());
            //课程有效期，开始时间
            market.setStartTime(courseMarket.getStartTime());
            //课程有效期，结束时间
            market.setEndTime(courseMarket.getEndTime());
            market.setPrice(courseMarket.getPrice());
            market.setQq(courseMarket.getQq());
            market.setValid(courseMarket.getValid());
            courseMarketRepository.save(market);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 保存课程id和上传成功之后的图片地址的关联关系
    @Transactional
    public ResponseResult saveCoursePic(String courseId, String pic) {
        // 一个课程只能有一张对应的图片
        // 我们获取课程id,如果有课程id就更新,没有的话,就插入两者的关联
        CoursePic coursePic = null;
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()){
            coursePic = optionalCoursePic.get();
        }
        if (coursePic == null){
            coursePic = new CoursePic();
        }

        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);

        coursePicRepository.save(coursePic);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 根据id查询课程相关的信息
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optionalPic = coursePicRepository.findById(courseId);
        if (optionalPic.isPresent()){
            CoursePic coursePic = optionalPic.get();
            return coursePic;
        }

        return null;
    }

    // 删除课程的图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        // 执行删除
        long count = coursePicRepository.deleteByCourseid(courseId);
        // 当影响到的记录数>0,表示删除成功
        if (count > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }

        return new ResponseResult(CommonCode.FAIL);
    }

    // 课程视图的查询 包括 基本信息 图片 课程营销计划等
    public CourseView courseview(String id) {
        CourseView courseView = new CourseView();

        // 查询课程的基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }

        // 查询课程的营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if(courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        // 查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    // 课程的预览
    public CoursePublishResult preview(String id) {
        // 根据id查询课程
        CourseBase courseBase = this.findCourseBaseById(id);
        // 准内CmsPage信息
        CmsPage cmsPage = new CmsPage();
        // 把配置文件中配置的信息写入到我们创建的对象中
        cmsPage.setSiteId(publish_siteId);  // 站点id
        cmsPage.setDataUrl(publish_dataUrlPre + id);  // 拼接获取数据模型的url
        cmsPage.setPageName(id + ".html");  // 页面名称
        cmsPage.setPageAliase(courseBase.getName());  // 设置页面别名,也就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);  // 设置物理路径
        cmsPage.setPageWebPath(publish_page_webpath);  // 页面的webPath
        cmsPage.setTemplateId(publish_templateId);  // 设置模板的id

        // 远程调用cms添加页面
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()){
            System.out.println("异常了!!!");
            // 如果不成功,就抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        // 如果成功就能获取到页面的id
        String pageId = cmsPageResult.getCmsPage().getPageId();

        // 拼装页面预览的url
        String URL = previewUrl + pageId;

        // 返回CoursePublishResult对象(对象中包含着拼装好的url)
        return new CoursePublishResult(CommonCode.SUCCESS,URL);
    }

    // 根据课程id查询课程的基本信息
    private CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    // 一键发布页面,要涉及到远程调用
    @Transactional  // 因为要更改课程的状态,所以要添加事务
    public CoursePublishResult publish(String id) {

        // 根据id查询课程
        CourseBase courseBase = this.findCourseBaseById(id);
        // 准内CmsPage信息
        CmsPage cmsPage = new CmsPage();
        // 把配置文件中配置的信息写入到我们创建的对象中
        cmsPage.setSiteId(publish_siteId);  // 站点id
        cmsPage.setDataUrl(publish_dataUrlPre + id);  // 拼接获取数据模型的url
        cmsPage.setPageName(id + ".html");  // 页面名称
        cmsPage.setPageAliase(courseBase.getName());  // 设置页面别名,也就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);  // 设置物理路径
        cmsPage.setPageWebPath(publish_page_webpath);  // 页面的webPath
        cmsPage.setTemplateId(publish_templateId);  // 设置模板的id

        // 远程调用cms的一键发布接口,将课程详情发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            // 说明发布失败,响应给前端
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        // 保存课程的发布状态为已经发布
        CourseBase baseChanged = this.saveCoursePubState(id);
        if (baseChanged == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        // 保存课程的索引信息
        // 先创建一个coursePub对象
        CoursePub coursePub = this.createCoursePub(id);

        // 将coursePub保存到数据库(logstash采集数据创建索引库)
        this.saveCoursePub(id, coursePub);

        // 向teachplanMediaPub中保存课程的媒资信息(logstash采集数据创建索引库)
        this.saveTeachplanMediaPub(id);

        // 缓存课程的信息

        return new CoursePublishResult(CommonCode.SUCCESS,cmsPostPageResult.getPageUrl());
    }

    // 向teachplanMediaPub中保存课程的媒资信息
    private void saveTeachplanMediaPub(String courseId){
        // 先删除TeachplanMediaPub中的数据
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        // 从TeachplanMedia中查询数据
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        // 将teachplanMediaList 插入到 TeachplanMediaPub 表中
        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();

        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            // 拷贝数据到新创建的teachplanMediaPub对象中去(两个属性就差着时间戳属性)
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            // 添加时间戳(为了采集数据而使用的)
            teachplanMediaPub.setTimestamp(new Date());

            teachplanMediaPubs.add(teachplanMediaPub);
        }

        // 将数据插入保存
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }

    // 更改课程的状态(改为已发布) 数据字典中对应的 代码号为 202002
    private CourseBase saveCoursePubState(String courseId){
        // 先查询出基本课程的信息,然后更改
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布状态
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    // 创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            // 基础信息CoursePub的属性中包含CourseBase的属性,因此可以直接拷贝
            BeanUtils.copyProperties(courseBase, coursePub);
        }

        // 查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        // 课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        // 课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);

        return coursePub;

    }

    // 将coursePub对象保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){

        CoursePub coursePubNew = null;

        // 如果数据库中有 CoursePub 对象就更新,没有的话就进行添加
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(id);
        if (optionalCoursePub.isPresent()){
            coursePubNew = optionalCoursePub.get();
        } else{
            // 如果根据id查不到的话,就新创建一个对象
            coursePubNew = new CoursePub();
        }

        // 将CoursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub,coursePubNew);
        // 为了防止传入的coursePub对象中id值为空,我们重新设置一下id值
        coursePubNew.setId(id);
        // 时间戳 为了方便 logstash来采集数据
        coursePubNew.setTimestamp(new Date());
        // 发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        // 设置更新时间
        coursePubNew.setPubTime(date);

        // 调用方法,保存对象
        CoursePub save = coursePubRepository.save(coursePubNew);
        return save;
    }

    // 保存课程计划与媒资文件的关联
    public ResponseResult saveCourseWithMedia(TeachplanMedia teachplanMedia) {

        // 进行关键参数的校验
        if (teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            // 返回非法参数
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }
        // 校验课程计划是否是第3等级的计划(只有最后一层的课程计划才能关联课程)
        String teachplanId = teachplanMedia.getTeachplanId();
        // 根据课程id获取出课程对象
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()){
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }
        Teachplan teachplan = teachplanOptional.get();
        // 取出课程的等级
        String grade = teachplan.getGrade();
        if (grade == null || !"3".equals(grade)){
            // 只允许选择第三级别的课程计划
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        // 查询TeachplanMedia
        Optional<TeachplanMedia> optionalMedia = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if (optionalMedia.isPresent()){
            // 能查询到就更新
            one = optionalMedia.get();
        } else {
            // 如果根据id查询不到,就创建一个新的对象
            // 如果查询不到,就插入
            one = new TeachplanMedia();
        }

        // 将TeachplanMedia对象保存到数据库
        one.setCourseId(teachplan.getCourseid());
        // 媒资文件的id
        one.setMediaId(teachplanMedia.getMediaId());
        // 媒资文件的原始名称
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        // 教学计划的id
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
