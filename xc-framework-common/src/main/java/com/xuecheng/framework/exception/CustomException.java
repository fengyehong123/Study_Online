package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

// 自定义异常类型,继承 RuntimeException运行时的异常,不会对代码增加侵入性
public class CustomException extends RuntimeException {

    // 错误代码
    private ResultCode resultCode;

    // 通过构造方法指定错误代码
    public CustomException(ResultCode resultCode){
        this.resultCode = resultCode;
    }

    // 获取错误代码
    public ResultCode getResultCode() {
        return resultCode;
    }

}
