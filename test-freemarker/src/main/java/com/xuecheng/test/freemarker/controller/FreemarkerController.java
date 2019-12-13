package com.xuecheng.test.freemarker.controller;

import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequestMapping("/freemarker")
@Controller  // 需要使用Controller,会输出html网页 不能使用@RestController,因为它会输出json数据
public class FreemarkerController {

    // 注入restTemplate
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/test1")
    public String test1(Map<String, Object> map){

        // map格式的数据作为形参
        // request域可以从map格式的数据中获取数据
        map.put("name","黑马程序员");

        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMondy(1000.86f);
        stu1.setBirthday(new Date());

        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMondy(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());

        List<Student> friends = new ArrayList<>();
        friends.add(stu1);

        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);

        // 把学生对象放在列表中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向数据模型放数据
        map.put("stus",stus);

        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);

        //向数据模型放数据
        map.put("stu1",stu1);
        //向数据模型放数据
        map.put("stuMap",stuMap);

        map.put("point", 102920122);

        //返回模板文件名称
        return "a/test1";
    }


    @RequestMapping("/banner")
    public String index_banner(Map<String, Object> map){

        // 使用restTemplate请求轮播图的模型数据
        // url 轮播图地址的接口
        ResponseEntity<Map> template = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        // 获取模型数据
        Map body = template.getBody();
        // 把body中所有的key/value都放在map中返回到前端接收
        map.putAll(body);

        return "a/index_banner";
    }

    @RequestMapping("/course")
    public String course(Map<String, Object> map){

        ResponseEntity<Map> template = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e581617f945f01617f9dabc40000", Map.class);
        // 获取模型数据
        Map body = template.getBody();
        // 把body中所有的key/value都放在map中返回到前端接收
        map.putAll(body);

        return "a/course";
    }
}