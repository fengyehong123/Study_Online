package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.reponse.GetMediaResult;
import com.xuecheng.framework.domain.learning.reponse.LearningCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.learning.client.CourseSearchClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseLearningService {

    @Autowired
    private CourseSearchClient courseSearchClient;

    public GetMediaResult getMedia(String courseId, String teachplanId) {

        // 校验学生学习的权限....

        // 远程调用
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);

        // 判断获取的对象是否为空或者媒资URL为空
        if(teachplanMediaPub == null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取视频播放地址出错
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }

        // 返回视频的播放地址
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());
    }
}
