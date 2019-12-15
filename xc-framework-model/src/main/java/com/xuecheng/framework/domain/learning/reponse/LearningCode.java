package com.xuecheng.framework.domain.learning.reponse;

import com.xuecheng.framework.model.response.ResultCode;

public enum LearningCode implements ResultCode {

    LEARNING_GETMEDIA_ERROR(false,23001,"获取学习地址失败"),
    CHOOSECOURSE_USERISNULL(false, 23002, "获取用户失败"),
    CHOOSECOURSE_TASKISNULL(false, 23003, "获取任务失败");

    boolean success;
    int code;
    String message;

    LearningCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
