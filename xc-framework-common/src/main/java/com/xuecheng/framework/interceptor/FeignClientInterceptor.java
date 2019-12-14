package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

// feign拦截器,用来解决被授权控制的微服务之间无法远程调用的问题
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {

        // 获取HttpRequest对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null){
            HttpServletRequest request = requestAttributes.getRequest();
            // 取出当前请求的header,找到jwt令牌
            // 获取出所有header的key的列表
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames!= null){
                while (headerNames.hasMoreElements()){
                    // 获取出header的key
                    String headerKey = headerNames.nextElement();
                    // 根据header的key,获取出header的值
                    String headerValue = request.getHeader(headerKey);

                    // 将jwt令牌(Header)向下传递
                    requestTemplate.header(headerKey,headerValue);
                }
            }
        }


    }
}
