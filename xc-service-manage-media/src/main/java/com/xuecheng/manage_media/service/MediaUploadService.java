package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;
    // 注入文件上传的地址
    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    // 从配置文件中获取路由key
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routeKey;

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    // 文件上传之前的准备工作
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        // 1.检查文件在磁盘上是否存在
        // 获取文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        // 获取文件所在的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);

        // 2. 键盘文件信息在mongoDB中是否存在 主键就是md5值
        Optional<MediaFile> optionalFile = mediaFileRepository.findById(fileMd5);
        // 如果文件路径存在,并且MongoDB中也能查询到数据的话
        if (file.exists() && optionalFile.isPresent()){
            // 文件存在,抛出异常告知文件已经存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        // 如果文件不存在,就检查文件所在的目录是否存在,如果不存在,就进行创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()){
            // 根据文件路径对象创建文件夹
            fileFolder.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 检查分块文件,前端根据检查的结果来决定是否继续上传分块
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        // 检查分块文件是否存在
        // 得到分块文件的所在目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        // 块文件的路径就在块文件目录的下面
        File file = new File(chunkFileFolderPath + chunk);
        if (file.exists()){
            // 若文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        } else {
            // 块文件不存在
            return new CheckChunkResult(CommonCode.FAIL,false);
        }
    }

    // 上传分块
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5){
        // 检查分块的目录,如果不存在就自动创建
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        // 得到分块文件的路径
        String chunkFilePath =  chunkFileFolderPath + chunk;
        File file1 = new File(chunkFileFolderPath);

        if (!file1.exists()){
            // 如果文件不存在就创建
            file1.mkdirs();
        }

        // 得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            // 通过流拷贝的方式,把输入流拷贝到输出流
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 拷贝完成之后关闭流
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 合并文件
    @Transactional
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        // 1.合并文件
        // 得到分块文件的所属目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFile = new File(chunkFileFolderPath);
        // 获取目录中的所有文件(分块文件列表)
        File[] files = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(files);

        // 创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        // 执行文件的合并,获取文件合并之后文件对象
        mergeFile = this.mergeFile(fileList, mergeFile);

        if (mergeFile == null){
            // 合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        // 2.校验文件的md5值是否和前端传入的md5一致
        boolean result = this.checkFileMd5(fileMd5, mergeFile);
        if (!result){
            // 如果校验的结果是false则校验失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        // 3.将文件的信息写入MongoDB
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        // 保存文件的相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        // 文件上传成功之后向mq消息队列发送消息
        this.sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);

    }

    // 发送视频处理消息 参数:文件上传成功之后的id
    public ResponseResult sendProcessVideoMsg(String mediaId){

        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if (!optionalMediaFile.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }

        // 构造消息的内容格式
        Map<String, String> map = new HashMap<>();
        map.put("mediaId", mediaId);
        String jsonStr = JSON.toJSONString(map);

        // 向mq发送视频处理消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routeKey,jsonStr);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

        // 向MQ发送视频处理消息
        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 得到文件所需目录的路径
    private String getFileFolderPath(String fileMd5){
        // 磁盘的路径 + md5的第一个字符 + md5的第二个字符 + md5本身
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
    }

    // 拼接得到文件所在的路径
    private String getFilePath(String fileMd5,String fileExt){
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    // 得到块文件所在目录
    private String getChunkFileFolderPath(String fileMd5){
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/chunk/";
    }

    // 合并文件
    /**
     *
     * @param chunkFileList 分块文件列表
     * @param mergeFile  合并文件对象
     * @return
     */
    private File mergeFile(List<File> chunkFileList,File mergeFile){
        try {
            // 判断要合并的文件是否存在
            if (mergeFile.exists()){
                // 如果文件存在,就删除,因为我们要重新写入
                mergeFile.delete();
            } else {
                // 如果要合并的文件不存在,我们就创建一个文件
                mergeFile.createNewFile();
            }

            // 对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName()) ){
                        return 1;
                    }
                    return -1;
                }
            });

            // 创建一个写对象 要向合并文件对象中写入文件
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] bytes = new byte[1024];  // 创建一个缓冲区域
            for (File file : chunkFileList) {
                // 创建一个读对象
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = raf_read.read(bytes)) != -1){
                    raf_write.write(bytes,0,len);
                }
                raf_read.close();
            }
            raf_write.close();

            // 返回合并后的文件
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 校验文件
    private boolean checkFileMd5(String fileMd5,File mergeFile){
        try {
            // 创建文件的输入流
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            // 获取合并成功之后文件的md5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (fileMd5.equalsIgnoreCase(md5Hex)){
                return true;
            }
            // 和前端的md5进行比较
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
