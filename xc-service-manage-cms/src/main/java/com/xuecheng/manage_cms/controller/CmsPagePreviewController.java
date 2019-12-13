package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private PageService pageService;

    // 页面预览
    @RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
    public void preview(@PathVariable("pageId")String pageId){

        // 执行页面静态化,获取文本格式的静态化页面
        String pageHtml = pageService.getPageHtml(pageId);
        if(StringUtils.isNotEmpty(pageHtml)){
            try {
                // response 是 父类BaseController 中封装的对象
                ServletOutputStream outputStream = response.getOutputStream();

                /*
                * 由于Nginx先请求cms的课程预览功能得到html页面，再解析页面中的ssi标签，这里必须保证cms页面预览返回的
                * 页面的Content-Type为text/html;charset=utf-8
                * 返回的页面用到了ssi功能的时候,才会需要添加下面的代码,我们之前的轮播图功能并没有使用到ssi,所以不添加也可以
                * 但是课程的详情页面静态化用到了,这一个静态化页面是由若干个页面拼凑而成,因此用到了
                * */
                response.setHeader("Content-type","text/html;charset=utf-8");
                // 直接把静态化之后的文本格式的数据返回给前端
                outputStream.write(pageHtml.getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
