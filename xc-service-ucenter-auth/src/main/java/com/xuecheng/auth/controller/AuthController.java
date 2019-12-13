package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.ext.UserToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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

    // 用户退出登录
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        // 从cookie中获取用户的令牌
        String token = this.getTokenFormCookie();

        // 删除redis中的token
        boolean result = authService.deleteFromRedis(token);

        // 清除cookie
        this.clearCookie(token);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 根据cookie中的信息查询用户jwt令牌
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {

        // 获取出cookie当中的用户身份令牌
        String uid = this.getTokenFormCookie();
        if (StringUtils.isEmpty(uid)){
            // 如果通过uid为空,说明cookie为空,说明用户未登录,用户未登录不算异常,因此不能抛出异常
            return new JwtResult(CommonCode.FAIL,null);
        }

        // 用获取出的用户身份令牌查询jwt令牌
        AuthToken jwtToken = authService.getJwtToken(uid);
        if (jwtToken!=null){
            String jwt_token = jwtToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }

    // 获取出cookie中的身份令牌
    private String getTokenFormCookie(){
        // 获取出 HttpServletResponse 对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        // 向cookie存入值的时候,写入的就是 uid
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map != null && map.get("uid") != null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }

    // 定义一个私有方法,将token保存到cookie中
    private void saveTokenToCookie(String token){

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();

        // HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        // boolean httpOnly ==> 值设置为 false 表示浏览器可以获取cookie
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token, cookieMaxAge,false);
    }

    // 删除cookie中的token
    private void clearCookie(String token){

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        // 删除cookie 把有效期改为 0
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token, 0,false);
    }
}
