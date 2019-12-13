package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    // 创建jwt
    @Test
    public void testCreateJwt(){

        // 通过 java自带的keytool命令生密钥库文件
        // keytool -genkeypair -alias xckey -keyalg RSA -keypass xuecheng -keystore xc.keystore -storepass xuechengkeystore

        // 指定密钥库文件
        String keyStore = "xc.keystore";
        // 指定密钥库密码
        String keyStore_password = "xuechengkeystore";
        // 指定密钥库文件的路径 new ClassPathResource() ==> Spring自带的对象,用于指定resources文件夹下的文件
        ClassPathResource classPathResource = new ClassPathResource(keyStore);
        // 密钥的别名
        String alias = "xckey";
        // 密钥的访问密码
        String key_password = "xuecheng";

        // 创建密钥工厂 指定密钥库文件和密钥库密码
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keyStore_password.toCharArray());
        // 根据密钥的别名和密钥对应的的密码 得到 密钥对 里面有公钥和私钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        // 获取到私钥(RSA私钥)
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        // jwt令牌的中包含的内容
        Map<String, String> body = new HashMap<>();
        body.put("name","itcast");
        String jsonStr = JSON.toJSONString(body);
        // 根据私钥生成jwt令牌 JwtHelper ==> 安全框架提供的生成jwt签名令牌的工具类
        // 通过Rsa算法进行签名,需要指定私钥
        Jwt jwt = JwtHelper.encode(jsonStr, new RsaSigner(aPrivate));
        // 生成jwt令牌的编码
        String jwtEncoded = jwt.getEncoded();
        System.out.println(jwtEncoded);

    }

    // 校验令牌
    @Test
    public void testVerify(){

        // 公钥
        String publishKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        // 需要校验的JWT令牌
        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.lQOqL1s4DpDHROUAibkz6EMf6hcM7HmTPgmg-SlkacVoQAV7y3XQ7LXxiua6SJlN_uNX_EFjzIshEg_kyy972DtymtRMc2NIO5HzIF5I4oQCxNPsJdhu6qQni6sTas3q0JbAarMZSajDX7HhzVSYWPQJCussA4e1r9oFxDcoAo6TEAXOW8gRHzNIygQz1yCj6mdf4UOHI070kRy7f3BdhmrUJdOuDIMoRBYS4WsEOibAU1UCNPaJAXpZC0ihrtdY7SCg1N43fimeFOHrfpLb6OmRF7v7uvGMgrhg9JIYDbJ6nbode5OJkNceRx8QUICre2yKAe0ctlvXO0REf6OpRA";
        // 校验jwt
        Jwt jwt = JwtHelper.decodeAndVerify(jwtToken, new RsaVerifier(publishKey));
        // 获取jwt令牌中自定义的内容
        String claims = jwt.getClaims();
        System.out.println(claims);  // {"name":"itcast"}  获取到我们自定义的内容
        // jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
