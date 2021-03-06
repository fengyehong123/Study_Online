package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 身份校验过滤器
@Component  // 这个过滤器是Spring容器的一个Bean
public class LoginFilter extends ZuulFilter {

    @Autowired
    private AuthService authService;

    // 设置过滤器的类型
    /*
     pre：请求在被路由之前执行
     routing：在路由请求时调用
     post：在routing和error过滤器之后调用
     error：处理请求时发生错误调用
    */
    @Override
    public String filterType() {
        // 用户的请求在被转发到微服务之前就要进行过滤
        return "pre";
    }

    // 过滤器的序号,序号越小,就越先被执行
    @Override
    public int filterOrder() {
        return 0;
    }

    // 如果为true,则表示要执行此过滤器
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // 进行具体的过滤
    // 测试的需求: 过滤所有请求，判断头部信息是否有Authorization，如果没有则拒绝访问，否则转发到微服务
    @Override
    public Object run() throws ZuulException {

        // 通过zuul提供的上下文对象获取到request
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        // 得到response
        HttpServletResponse response = currentContext.getResponse();

        // 获取cookie中的身份令牌
        String cookie = authService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(cookie)){
            // 调用拒绝访问方法
            this.access_denied();
            return null;
        }

        // 从head中取出jwt
        String jwtHeader = authService.getJwtHeader(request);
        if (StringUtils.isEmpty(jwtHeader)){
            // 调用拒绝访问方法
            this.access_denied();
            return null;
        }

        // 从redis中获取令牌的过期时间
        long time = authService.getTokenTimeFromRedis(cookie);
        if (time < 0){
            // 调用拒绝访问方法
            this.access_denied();
            return null;
        }

        return null;
    }

    // 拒绝访问方法
    private void access_denied(){

        // 获取request和response对象
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();
        // 拒绝向后继续访问
        currentContext.setSendZuulResponse(false);
        // 设置响应状态码
        currentContext.setResponseStatusCode(200);
        // 构建响应的信息
        ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED);
        // 把响应信息对象转换为json串
        String jsonString = JSON.toJSONString(unauthenticated);
        currentContext.setResponseBody(jsonString);
        // 转成json，设置contentType
        response.setContentType("application/json;charset=utf-8");
    }
}
