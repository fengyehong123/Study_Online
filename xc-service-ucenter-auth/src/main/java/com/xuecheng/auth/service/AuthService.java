package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired  // 远程调用
    private RestTemplate restTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${auth.tokenValiditySeconds}")
    private int tokenValiditySeconds;

    // 用户认证来申请令牌,同时将获取的令牌存储到redis
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        // 远程请求SpringSecurity申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        // 获取用户身份的令牌
        String access_token = authToken.getAccess_token();
        // jwt令牌转换为json保存到redis中
        String strJWT = JSON.toJSONString(authToken);

        // 存储令牌到redis 指定key value 过期时间
        boolean b = this.saveToken(access_token, strJWT, tokenValiditySeconds);
        // 如果储存到redis失败
        if (!b){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        // 如果没有异常,就把JWt令牌对象返回
        return authToken;
    }

    // 申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        // 从eureka中获取认证服务的地址(因为SpringSecurity在认证服务中)
        // 指定微服务的名称,从注册中心获取微服务的实例
        ServiceInstance choose = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        // 此地址就是 http://ip:port
        URI uri = choose.getUri();

        // 拼接申请令牌的地址(路径是SpringSecurity规定好的)
        String authUrl = uri + "/auth/oauth/token";

        // 定义header 其中包含了 http basic认证信息
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        // 获取httpBasic的串
        String httpBasic = this.getHttpBasic(clientId,clientSecret);
        // 将 httpBasic 添加到头信息中
        header.add("Authorization",httpBasic);

        // 定义body 包括：grant_type、username、passowrd
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body, header);

        // 指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });

        // 指定远程调用的地址 请求的方法 方法中放置的参数 返回值的类型
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        // 申请的令牌的信息
        Map bodyMap = exchange.getBody();
        // 判断申请到的令牌是否有存在为空的情况
        if (bodyMap == null || bodyMap.get("access_token") == null || bodyMap.get("refresh_token") == null || bodyMap.get("jti") == null){
            return null;
        }

        AuthToken authToken = new AuthToken();
        // 访问令牌(jwt)
        String jwt_token = (String) bodyMap.get("access_token");
        // 刷新令牌(jwt)
        String refresh_token = (String) bodyMap.get("refresh_token");
        // jti，作为用户的身份标识
        String access_token = (String) bodyMap.get("jti");  // 访问的短令牌
        authToken.setJwt_token(jwt_token);
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);

        // 把封装好的令牌对象返回
        return authToken;
    }


    // 存储令牌到redis
    /*
     * @param access_token 用户身份令牌
     * @param content  AuthToken 所对应的内容
     * @param ttl  过期时间
     */
    private boolean saveToken(String access_token,String content,long ttl){

        // 令牌名称
        String key = "user_token:" + access_token;
        // 存令牌到redis
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        // 存进去之后,通过key查询过期时间
        Long expire = stringRedisTemplate.getExpire(key);

        // 如果时间大于0,就代表成功
        return expire > 0;
    }


    // 获取httpBasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        // 将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        // 进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        // 转换为字符串
        String s = new String(encode);
        // 注意: 有一个空格
        return "Basic " + s;
    }
}
