package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

public class ExceptionCast {

    // 传入错误代码,然后抛出异常
    public static void cast(ResultCode resultCode){
        throw new CustomException(resultCode);
    }
}
