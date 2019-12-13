package com.xuecheng.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@SpringBootTest(classes = FreeMarkTest.class)
@RunWith(SpringRunner.class)
public class FreeMarkTest {

    // 测试静态化,基于ftl模板文件生成html文件
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        // 定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());

        // 定义模板
        // 设置模板目录,得到classpath的路径,定义模板路径
        String path = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(path + "/templates/a/"));

        // 获取模板文件的内容
        Template template = configuration.getTemplate("test1.ftl");

        // 定义数据模型
        Map map = getMap();

        // 静态化,得到静态化后的字符串
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        // 根据阿帕奇的工具类获取一个输入流
        InputStream inputStream = IOUtils.toInputStream(content);
        // 定义一个输出流
        FileOutputStream outputStream = new FileOutputStream(new File("d:/test1.html"));
        //输入文件,把输入流的文件拷贝到输出流进行输出
        IOUtils.copy(inputStream,outputStream);

        // 关闭输入和输出流
        inputStream.close();
        outputStream.close();

    }

    // 根据模板文件的内容(字符串)生成html文件
    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException{
        // 定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());

        // 定义模板.模板的内容,使用简单的字符串作为模板
        String templateString="" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " 名称：${name}\n" +
                " </body>\n" +
                "</html>";

        // 使用模板加载器,将字符串变为模板
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        // 在配置类中添加模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        // 通过配置类获取模板对象  指定文件编码
        Template template = configuration.getTemplate("template", "utf-8");

        // 定义数据模型
        Map map = getMap();

        // 静态化,得到静态化后的字符串
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        // 根据阿帕奇的工具类获取一个输入流
        InputStream inputStream = IOUtils.toInputStream(content);
        // 定义一个输出流
        FileOutputStream outputStream = new FileOutputStream(new File("d:/test2.html"));
        //输入文件,把输入流的文件拷贝到输出流进行输出
        IOUtils.copy(inputStream,outputStream);

        // 关闭输入和输出流
        inputStream.close();
        outputStream.close();
    }

    // 定义一个数据模型
    public Map getMap(){
        HashMap<Object, Object> map = new HashMap<>();

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

        return map;
    }

}
