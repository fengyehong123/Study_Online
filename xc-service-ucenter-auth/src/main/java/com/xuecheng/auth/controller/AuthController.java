package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController implements AuthControllerApi {

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Autowired
    private AuthService authService;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {

        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if (loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }

        // 用户账号和密码
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 申请令牌(返回一个用户存储令牌信息的对象)
        AuthToken authToken = authService.login(username,password,clientId, clientSecret);
        // 获取短令牌,用户身份令牌(占用cookie资源比较少)
        String token = authToken.getAccess_token();

        // 将令牌储存到cookie
        this.saveTokenToCookie(token);

        return new LoginResult(CommonCode.SUCCESS,token);
    }

    // 定义一个私有方法,将token保存到cookie中
    private void saveTokenToCookie(String token){

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();

        // HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        // boolean httpOnly ==> 值设置为 false 表示浏览器可以获取cookie
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token, cookieMaxAge,false);
    }


    @Override
    public ResponseResult logout() {
        return null;
    }
}
