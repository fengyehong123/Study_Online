package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        if (queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        // 条件值对象
        MediaFile mediaFile = new MediaFile();
        // 只有当传来的搜索对象中的条件不为空的时候,我们才进行条件的拼接
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }

        // 条件匹配器(标签和原始名称都是模糊查询)  默认都是精确匹配,所以处理的状态不用添加精确匹配的条件也可以
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("tag",ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("fileOriginalName",ExampleMatcher.GenericPropertyMatchers.contains());

        // 定义Example条件查询对象 条件值对象 + 匹配器
        Example<MediaFile> mediaFileExample = Example.of(mediaFile,exampleMatcher);

        // 分页查询对象
        if (page <= 0){
            page = 1;
        }
        page = page - 1;
        if (size <= 0){
            size = 10;
        }
        // 分页查询对象
        Pageable pageObj = PageRequest.of(page, size);
        Page<MediaFile> all = mediaFileRepository.findAll(mediaFileExample, pageObj);
        // 获取总的记录数
        long totalElements = all.getTotalElements();
        // 获取数据列表
        List<MediaFile> mediaFileList = all.getContent();

        QueryResult<MediaFile> queryResult = new QueryResult<>();
        queryResult.setList(mediaFileList);
        queryResult.setTotal(totalElements);

        // 返回的结果
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }
}
