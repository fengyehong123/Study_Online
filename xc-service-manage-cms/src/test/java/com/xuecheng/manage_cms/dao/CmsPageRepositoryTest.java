package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsPageParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    // 测试查询全部
    @Test
    public void testFindAll(){
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    // 分页查询
    @Test
    public void testFindPage(){
        int page = 0;  // 查询的页数 ,从第0页开始
        int size = 5;  // 每页显示的数量
        Pageable of = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(of);
        System.out.println(all);
    }

    // 向MongoDB数据库中添加数据
    @Test
    public void testInsert(){
        //定义实体类
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageName("测试页面");
        cmsPage.setPageCreateTime(new Date());
        List<CmsPageParam> cmsPageParams = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        cmsPageParams.add(cmsPageParam);
        cmsPage.setPageParams(cmsPageParams);

        // 把实体类添加到数据库中
        cmsPageRepository.save(cmsPage);

    }

    // 删除数据库中的数据
    @Test
    public void testDelete() {
        cmsPageRepository.deleteById("5dde261cc4913b4f28dee05a");
    }

    // 修改数据库中的数据
    @Test
    public void testUpdate(){
        // 查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5dde2800c4913b3a04a15c3a");
        /*
        *  Optional是jdk1.8引入的类型，Optional是一个容器对象，它包括了我们需要的对象，使用isPresent方法判断所包
            含对象是否为空，isPresent方法返回false则表示Optional包含对象为空，否则可以使用get()取出对象进行操作。
            Optional的优点是：
            1、提醒你非空判断。
            2、将对象非空检测标准化。
        * */
        if (optional.isPresent()){
            // 如果没有空指针问题,通过get方法取出 CmsPage对象
            CmsPage cmsPage = optional.get();

            // 设置要修改的值
            cmsPage.setPageAliase("修改之后");

            // 保存到数据库
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }

    // 根据页面名称来查询
    @Test
    public void findByPageName(){
        CmsPage name = cmsPageRepository.findByPageName("测试页面");
        System.out.println(name);
    }

    // 自定义条件进行查询
    @Test
    public void testFindAllByExample(){
        // 分页参数
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        // 条件值对象,把查询的条件封装成一个对象进行封装
        CmsPage cmsPage = new CmsPage();
        // cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        cmsPage.setPageAliase("轮播");
        // 条件匹配器,添加搜索的条件
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        // 只要该字段(pageAliase)包含 要搜索的关键词
        // 使用匹配器必须要返回一个对象,否则无法使用
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        // 定义Example
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();

        System.out.println(content);
    }

}
