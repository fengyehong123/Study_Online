package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// 异常捕获类,用来捕获抛出的异常
@ControllerAdvice  // 控制器增强类
public class ExceptionCatch {

    // 日志类对象
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);
    // 定义map,配置异常类型所对应的错误代码,这个Map是谷歌提供的 数据不可修改且基于线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    // 定义map的builder对象,去构建 ImmutableMap
    private static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    // 获取自定义CustomException此类异常
    // @ExceptionHandler 被这个注解标记的类,会自动捕获自定义的异常
    @ExceptionHandler(CustomException.class)
    @ResponseBody  // 把错误信息给转换成json格式返回到前端
    public ResponseResult customException(CustomException customException){

        // 记录日志
        LOGGER.error("catch exception:{}",customException.getMessage());

        // 获取到自定义的异常类对象,然后获取出异常信息
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

    // 捕获框架产生的Exception异常
    @ExceptionHandler(Exception.class)
    @ResponseBody  // 把错误信息给转换成json格式返回到前端
    public ResponseResult exception(Exception exception){

        // 记录日志
        LOGGER.error("catch exception:{}",exception.getMessage());
        if (EXCEPTIONS == null){
            // 构建 EXCEPTIONS
            EXCEPTIONS = builder.build();
        }
        // 从EXCEPTIONS中寻找异常类型对应的错误代码,如果找到了错误代码就响应给用户,如果找不到,就给用户响应9999异常
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if (resultCode != null){
            // 如果未知异常是我们预计好的,就返回预计好的异常
            return new ResponseResult(resultCode);
        } else {
            // 如果是未知异常,就返回未知异常9999给前端
            return new ResponseResult(CommonCode.SERVER_ERROR);
        }
    }

    static {
        // 定义系统级别异常类型所对应的错误代码
        // 参数1:错误类型 参数2:错误代码
        // 用postman测试,http://localhost:31001/cms/page/add put方法访问该地址,却不携带任何参数信息会引起下面的异常
        builder.put(HttpMessageNotReadableException.class,CommonCode.INCALID_PARAM);
    }
}

