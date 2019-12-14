package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsConfigRepository cmsConfigRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;

    // 页面查询
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){

        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }

        // 实现自定义查询
        // 条件匹配器 pageAliase 属性中包含就可以
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        // 条件值对象(站点id,模板id,页面别名 )
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        // 模板id作为查询条件
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        // 页面的别名
        if (StringUtils.isNotEmpty( queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        // 进行页面别名模板查询设置
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);


        // 对分页参数进行判断,如果页码<=0,就从第一页开始查询
        if (page <= 0){
            page = 1;
        }
        // 因为代码中,分页是从第0页开始的,所以用户指定的第1页,对代码来说是第0页
        page = page -1;

        if (size <=0){
            size = 10;
        }

        // 定义一个分页对象
        Pageable pageRequest = PageRequest.of(page, size);
        // 进行分页 + 条件查询
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageRequest);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);

        return queryResponseResult;
    }

    // 新增页面,不处理异常的情况
    /*public CmsPageResult add(CmsPage cmsPage){
        // 校验页面的名称,站点id,页面webpath的唯一性
        // 根据页面名称,站点id,页面的webpath去cms_page集合查询,如果能查到,说明页面存在,如果查询不到就继续添加

        // 调用dao,新增页面
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 == null){
            // 因为主键是数据库自增加的,为了安全起见,我们把主键设置为null
            cmsPage.setPageId(null);
            // 调用dao新增页面
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
        }
        // 如果页面已经存在,返回添加失败的请求
        return new CmsPageResult(CommonCode.FAIL,null);
    }*/

    // 新增加页面,处理异常.先判断可能出现的异常是否存在,如果不存在了再继续执行其他的代码
    public CmsPageResult add(CmsPage cmsPage){
       if (cmsPage == null){

       }

        // 调用dao,新增页面
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            // 页面已经存在,抛出异常,异常内容就是页面已经存在
            // 抛出我们自定义异常,使用自定义的错误状态码
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        // 因为主键是数据库自增加的,为了安全起见,我们把主键设置为null
        cmsPage.setPageId(null);
        // 调用dao新增页面
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
    }

    // 根据页面id查询页面
    public CmsPage getById(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }

        return null;
    }


    // 修改页面
    public CmsPageResult edit(String id, CmsPage cmsPage){
        CmsPage one = this.getById(id);
        if (one != null){
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());

            // 更新dataURL
            one.setDataUrl(cmsPage.getDataUrl());

            //执行更新
            CmsPage save = cmsPageRepository.save(one);
            if (save != null){
                // 返回成功
                CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, save);
                return cmsPageResult;
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    // 根据id删除页面
    public ResponseResult delete(String id){
        // 先查询要删除的页面是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }


    // 根据id查询cmsConfig
    public CmsConfig getConfigById(String id){

        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }

        return null;
    }

    // 页面静态化方法
    /*
     * 静态化程序获取页面的DataUrl
     *
     * 静态化程序远程请求DataUrl获取数据模型
     *
     * 静态化程序根据数据模型获取页面的模板信息
     *
     * 执行页面静态化
     * */
    public String getPageHtml(String pageId){

        // 根据dataUrl获取的model
        Map map = this.getModelByPageId(pageId);
        if (map == null){
            // 数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        // 获取页面的模板信息
        String templateContent = this.getTemplateByPageId(pageId);

        if (StringUtils.isEmpty(templateContent)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        // 开始执行静态化,把模板和数据结合到一起,形成静态的html页面(String格式)
        String htmlContent = this.generateHtml(templateContent, map);

        return htmlContent;
    }

    // 指定静态化的方法
    private String generateHtml(String templateContent,Map model){

        // 创建配置类对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        // 创建模板加载器(字符串)
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        // 向加载器中传入 模板名称和模板的内容
        stringTemplateLoader.putTemplate("template", templateContent);

        // 向配置类对象中配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);

        // 获取模板
        try {
            Template template = configuration.getTemplate("template");
            // 调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 有异常返回null
        return null;
    }

    // 获取页面的模板信息
    private String getTemplateByPageId(String pageId){

        // 获取页面的模板信息
        // 获取出页面的id
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            // 页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        // 查询模板的信息
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(templateId);
        if (byId.isPresent()){
            CmsTemplate cmsTemplate = byId.get();
            // 获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            // 根据模板文件id从GridFS中获取模板文件的内容

            // 根据id查询文件
            // Criteria 是一个条件对象,用来拼装where条件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            // 打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            // 创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            // 获取流中的数据,我们把数据转换为字符串(仅供测试时使用)
            try {
                String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                // 返回模板的内容
                return s;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // 获取数据模型
    private Map getModelByPageId(String pageId){
        // 获取出页面的id
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            // 页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 获取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            // 如果 dataUrl 为空,抛出异常
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        // 通过restTemplate远程调用来请求dataUrl来获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();

        return body;
    }


    // 页面的发布
    public ResponseResult post(String pageId){

        // 执行页面静态化(返回的数据 加载好数据之后的模板)
        String pageHtml = this.getPageHtml(pageId);

        // 将静态化的页面保存到GridFS中
        this.saveHtml(pageId, pageHtml);

        // 向消息队列中发送消息,消息队列会把静态化的文件保存到服务器
        this.sendPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 保存HTML到GridFs
    private CmsPage saveHtml(String pageId,String htmlContent){

        // 得到页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }
        ObjectId objectId = null;

        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            // 将html文件的内容保存到GridFS中 参数1: 文件内容的输入流 参数2: 文件的名称
            // 返回的是新的静态化之后的静态文件的id
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 此时cmsPage所对应的模板id需要更新了,将html文件的id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        // 保存更新最新的cmsP
        // age
        cmsPageRepository.save(cmsPage);

        return cmsPage;
    }

    // 向消息队列发送消息
    private void sendPostPage(String pageId){
        // 得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INCALID_PARAM);
        }

        // 创建消息对象
        Map<String, String> msg = new HashMap<>();
        msg.put("pageId", pageId);

        // 把Map格式转换为json串
        String jsonStr = JSON.toJSONString(msg);

        // 根据页面对象得到站点id
        String siteId = cmsPage.getSiteId();

        // 将消息发送给mq 指定交换机和routingKey和要发送的消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonStr);

    }

    // 如果有页面的话,就更新 没有页面的话,就保存
    public CmsPageResult save(CmsPage cmsPage) {

        // 根据页面的唯一索引判断页面是否存在(我们在MongoDB数据库中添加了一个以pageName和siteId和PageWebPath为基准的索引)
        CmsPage cmsPageObj = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),cmsPage.getSiteId(),cmsPage.getPageWebPath());
        if (cmsPageObj != null){
            // 调用方法进行更新
            // 原先的id配合新的内容
            return this.edit(cmsPageObj.getPageId(),cmsPage);
        }

        return this.add(cmsPage);
    }

    // 一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {

        // 将页面的信息存储到cms_page的集合中
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            // 如果储存不成功,就抛出异常
            ExceptionCast.cast(CommonCode.FAIL);
        }
        // 得到页面的id
        CmsPage cmsPageSave = cmsPageResult.getCmsPage();
        String pageId = cmsPageSave.getPageId();

        // 执行页面发布(先静态化,保存GridFS,向MQ发送消息)
        ResponseResult result = this.post(pageId);
        if (!result.isSuccess()){
            // 如果发布不成功,就抛出异常
            ExceptionCast.cast(CommonCode.FAIL);
        }

        // 页面发布成功之后,拼接能访问到这个静态html的url
        // 拼接url
        String siteId = cmsPageSave.getSiteId();  // 获取站点的id
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        // 获取站点的域名
        String siteDomain = cmsSite.getSiteDomain();
        // 获取web路径
        String siteWebPath = cmsSite.getSiteWebPath();
        // 获取页面的路径
        String pageWebPath = cmsPageSave.getPageWebPath();
        // 获取页面的名称
        String pageName = cmsPageSave.getPageName();
        // 拼接静态页面的web访问地址
        String pageUrl = siteDomain + siteWebPath + pageWebPath + pageName;

        // 返回能访问到静态化页面的url
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    // 根据站点的id获取站点的信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
