package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){

        // 定义key
        String key = "token_type:c0918412-50af-408f-bda3-1d8809e69b31";

        // 定义value
        Map<String, String> map = new HashMap<>();
        map.put("jwt","eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9");
        map.put("refresh_token","iwiY2xpZW50X2lkIjoiWGNXZWJBcHAifQ");
        // 把map转换为json字符串
        String jsonString = JSON.toJSONString(map);

        // 存储数据
        stringRedisTemplate.boundValueOps(key).set(jsonString,60, TimeUnit.SECONDS);

        // 获取数据
        String s = stringRedisTemplate.opsForValue().get(key);
        System.out.println(s);


    }
}
