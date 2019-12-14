package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExceptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

// 课程管理自定义的异常类型,定义异常类型所对应的的错误代码
// 继承我们自定义的异常捕获类
@ControllerAdvice
public class CustomExceptionCatch extends ExceptionCatch {

    static {
        // 除了CustomException以外的异常类型及对应的错误代码在这里定义,如果不定义则统一返回固定的错误信息
        // 返回权限不足,无权访问的异常
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
