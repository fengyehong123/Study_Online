package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "上传文件接口",description = "请求数据,负责上传文件")
public interface FileSystemControllerApi {

    /**
     * 上传文件
     * @param multipartFile 文件
     * @param filetag 文件标签
     * @param businesskey 业务key
     * @param metadata 元信息,json格式
     * @return
     */
    @ApiOperation("上传文件")  // 留出通用数据,供其他模块调用 String metadata 是字符串形式的json数据 我们可以把 json转Map数据
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata);
}
