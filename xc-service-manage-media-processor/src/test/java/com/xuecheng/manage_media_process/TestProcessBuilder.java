package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    // 使用processBuilder来调用第三方程序
    @Test
    public void testProcessBuilder() throws IOException {

        //创建ProcessBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 设置执行的第三方程序(命令)
        processBuilder.command("ipconfig");
        //processBuilder.command("ping","127.0.0.1");

        // processBuilder.command("java","-jar","f:/xc-service-manage-course.jar");
        // 将标准输入流和错误输入流合并，通过标准输入流读取信息就可以拿到第三方程序输出的错误信息、正常信息
        processBuilder.redirectErrorStream(true);

        // 启动一个进程
        Process process = processBuilder.start();
        // 由于前边将错误和正常信息合并在输入流，只读取输入流
        InputStream inputStream = process.getInputStream();
        // 将字节流转成字符流 为了防止乱码,我们添加一个编码
        InputStreamReader reader = new InputStreamReader(inputStream,"gbk");
       // 字符缓冲区
        char[] chars = new char[1024];
        int len = -1;
        while((len = reader.read(chars))!=-1){
            String string = new String(chars,0,len);
            // 打印获取到的信息
            System.out.println(string);
        }

        inputStream.close();
        reader.close();
    }

    // 测试使用工具类将avi转成mp4
    @Test
    public void testProcessMp4() throws IOException{
        //创建ProcessBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();

        // 设置执行的第三方程序(命令)
        ArrayList<String> command = new ArrayList<>();
        command.add("ffmpeg.exe");  // 因为我们配置了环境变量,因此可以直接输入命令执行操作
        command.add("-i");
        command.add("D:\\xuecheng_video\\lucene.avi");  // 指定原始文件
        command.add("-y");//覆盖输出文件
        command.add("-c:v");
        command.add("libx264");
        command.add("-s");
        command.add("1280x720");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-b:a");
        command.add("63k");
        command.add("-b:v");
        command.add("753k");
        command.add("-r");
        command.add("18");
        command.add("E:\\ffmpeg_test\\1.mp4");  // 指定输入文件
        
        processBuilder.command(command);

        // 将标准输入流和错误输入流合并，通过标准输入流读取信息就可以拿到第三方程序输出的错误信息、正常信息
        processBuilder.redirectErrorStream(true);

        // 启动一个进程
        Process process = processBuilder.start();
        // 由于前边将错误和正常信息合并在输入流，只读取输入流
        InputStream inputStream = process.getInputStream();
        // 将字节流转成字符流 为了防止乱码,我们添加一个编码
        InputStreamReader reader = new InputStreamReader(inputStream,"gbk");
        // 字符缓冲区
        char[] chars = new char[1024];
        int len = -1;
        while((len = reader.read(chars))!=-1){
            String string = new String(chars,0,len);
            // 打印获取到的信息
            System.out.println(string);
        }

        inputStream.close();
        reader.close();
    }

}
