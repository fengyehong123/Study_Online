package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class FileSystemService {

    @Autowired
    private FileSystemRepository fileSystemRepository;
    // 将配置文件中的fasfDFS信息注入到此处
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;

    // 上传文件
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata){

        if (multipartFile == null){
            // 上传的文件不存在
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }

        // 1. 将文件上传到fastDFS中,得到文件的id
        String fileId = this.uploadTofastDFS(multipartFile);
        if (StringUtils.isEmpty(fileId)){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        // 2. 将文件id以及其他的文件信息存储到mongodb中
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFiletag(filetag);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        // 文件的类型
        fileSystem.setFileType(multipartFile.getContentType());
        // metadata 是字符串格式的json数据,我们需要把字符串转换为json,然后转换为Map格式的数据,保存到数据库
        if (StringUtils.isNotEmpty(metadata)){
            Map map = null;
            try {
                map = JSON.parseObject(metadata, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                // 如果传入的数据不是字符串格式的json,就会转换失败,此时抛出异常
                ExceptionCast.cast(CommonCode.FAIL);
            }
            fileSystem.setMetadata(map);
        }
        fileSystemRepository.save(fileSystem);
        // 把上传成功的文件信息返回给前端展示
        return new UploadFileResult(CommonCode.SUCCESS,fileSystem);
    }

    // 上传文件到fastDFS
    /**
     * @param multipartFile 要上传的文件
     * @return 上传成功之后的文件id
     */
    private String uploadTofastDFS(MultipartFile multipartFile){
        // 初始化fastDFS的环境
        this.initFastConfig();
        // 创建一个trackerClient客户端
        TrackerClient trackerClient = new TrackerClient();
        try {
            // 获取tracker服务
            TrackerServer trackerServer = trackerClient.getConnection();
            // 获取storage服务
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            // 获取storageClient来上传文件 StorageClient1 是新版本
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
            // 得到文件的字节上传文件
            // 得到文件的字节
            byte[] bytes = multipartFile.getBytes();
            // 得到文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            // assert originalFilename != null;
            int lastIndex = originalFilename.lastIndexOf(".");
            // 获取到文件的扩展名
            String ext = originalFilename.substring(lastIndex+1);
            // 文件的字节信息 文件的扩展名 文件的元信息(可以不管的) 返回上传成功的文件id
            String fileId = storageClient1.upload_file1(bytes, ext, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 初始化fastDFS环境
    private void initFastConfig(){
        // 初始化tracker服务器地址(多个tracker中间以英文状态下的逗号作为分隔)
        try {
            // 因为是从配置文件中分别获取的配置信息,我们我们在此处分别进行配置,而不是导入整个配置文件
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_charset(charset);
        } catch (Exception e) {
            e.printStackTrace();
            // 报错的话,就抛出异常 初始化环境出错
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

}
