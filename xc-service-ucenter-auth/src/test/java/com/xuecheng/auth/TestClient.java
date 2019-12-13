package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired  // 远程调用
    private RestTemplate restTemplate;

    // 远程请求SpringSecurity来获取令牌
    @Test
    public void testClient(){

        // 从eureka中获取认证服务的地址(因为SpringSecurity在认证服务中)
        // 指定微服务的名称,从注册中心获取微服务的实例
        ServiceInstance choose = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        // 此地址就是 http://ip:port
        URI uri = choose.getUri();

        // 拼接申请令牌的地址
        String authUrl = uri + "/auth/oauth/token";

        // 定义header 其中包含了 http basic认证信息
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        // 获取httpBasic的串
        String httpBasic = this.getHttpBasic("XcWebApp", "XcWebApp");
        // 将 httpBasic 添加到头信息中
        header.add("Authorization",httpBasic);

        // 定义body 包括：grant_type、username、passowrd
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type","password");
        body.add("username","itcast");
        body.add("password","123");

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
        System.out.println(bodyMap);


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
