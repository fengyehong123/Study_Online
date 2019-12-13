package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Autowired
    private MediaFileRepository mediaFileRepository;
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;
    @Value("${xc-service-manage-media.video-location}")
    private String video_location;  // 视频的地址

    // 指定监听的队列名称,把配置的容器工厂给添加到此处,用于处理视频的并发
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){

        // 1. 解析消息内容,得到mediaId(消息内容是字符串格式的json数据)
        Map map = JSON.parseObject(msg, Map.class);  // 将字符串json转换为map
        String mediaId = (String) map.get("mediaId");

        // 2. 用mediaId从数据库查询文件信息
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if (!mediaFileOptional.isPresent()){
            // 此处不用抛出异常,因为是监听消息队列,如果获取不到数据,向后就不执行即可
            return;
        }
        MediaFile mediaFile = mediaFileOptional.get();
        // 获取到文件的类型(如果类型不是.avi就不进行转换)
        String fileType = mediaFile.getFileType();
        if (!"avi".equals(fileType)){
            // 如果视频格式不是 .avi 就把状态更改为 无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            // 视频需要处理,把视频的的状态更改为处理中
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        // 3. 使用工具类将avi文件生成mp4文件
        // 拼接要转换的.avi文件所在的目录
        String video_path = video_location + mediaFile.getFilePath() + mediaFile.getFileName();
        // 拼接转换之后的文件的名称
        String mp4_name = mediaFile.getFileName() + ".mp4";
        // 转换之后的mp4文件所在的目录
        String mp4folder_path = video_location + mediaFile.getFilePath();
        // 用工具类进行 .avi ==> .mp4
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !"success".equals(result)){
            // 说明处理失败
            mediaFile.setProcessStatus("303003");
            // 错误信息,记录失败的原因
            MediaFileProcess_m3u8 MediaFileM3u8 = new MediaFileProcess_m3u8();
            MediaFileM3u8.setErrormsg(result);
            // 将错误信息保存到对象中
            mediaFile.setMediaFileProcess_m3u8(MediaFileM3u8);
            mediaFileRepository.save(mediaFile);
            // 禁止执行
            return;
        }

        // 4. 将mp4文件生成m3u8和ts文件
        // 拼接转换之后的mp4文件的路径(和原.avi在同一个文件目录下)
        String mp4_video_path = video_location + mediaFile.getFilePath() + mp4_name;
        // m3u8_name文件名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        // m3u8_name文件所在的目录 转换后的文件都保存在 hls 目录下
        String m3u8folder_path = video_location + mediaFile.getFilePath() + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_video_path, m3u8_name, m3u8folder_path);
        // 生成m3u8和ts文件
        String res = hlsVideoUtil.generateM3u8();
        if (res == null || !"success".equals(res)){
            // 说明处理失败
            mediaFile.setProcessStatus("303003");
            System.out.println();
            // 错误信息,记录失败的原因
            MediaFileProcess_m3u8 MediaFileM3u8 = new MediaFileProcess_m3u8();
            MediaFileM3u8.setErrormsg(result);
            // 将错误信息保存到对象中
            mediaFile.setMediaFileProcess_m3u8(MediaFileM3u8);
            mediaFileRepository.save(mediaFile);
            // 禁止执行
            return;
        }

        // 记录处理成功状态值
        mediaFile.setProcessStatus("303002");
        // 获取生成的ts文件列表
        List<String> tsList = hlsVideoUtil.get_ts_list();
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(tsList);
        // 把生成的ts文件列表页保存到数据库中
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

        // 保存fileUrl(生成的m3u8文件所在的路径在线学习会读取该路径播放视频)
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);

        // 保存到数据库
        mediaFileRepository.save(mediaFile);
    }

}
