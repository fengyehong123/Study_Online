package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 从头部取出jwt令牌
    public String getJwtHeader(HttpServletRequest request){
        // 取出头部的信息
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isEmpty(authorization)){
            return null;
        }
        // jwt令牌 = "Bearer " + jwt信息
        if (!authorization.startsWith("Bearer ")){
            return null;
        }
        // 截取字符串,获取出jwt令牌
        String str = authorization.substring(7);
        return str;
    }


    // 从cookie取出token令牌
    public String getTokenFromCookie(HttpServletRequest request){

        Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
        String token = cookieMap.get("uid");
        if (StringUtils.isEmpty(token)){
            return null;
        }
        return token;
    }


    // 查询令牌的有效期
    public long getTokenTimeFromRedis(String access_token){
        // 拼接key
        String key = "user_token:" + access_token;
        // 根据key查询过期时间,指定秒为单位
        Long time = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        return time;
    }
}
