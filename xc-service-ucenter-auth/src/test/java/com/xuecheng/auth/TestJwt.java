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
        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE1NzYzMzY5OTMsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjQwNTJlY2ZlLTc3NTAtNDIwZS04MGIwLTE2NTZlNDgwNzBkMyIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.J523z3wuk1Y2WIHyKG203qZR4UHIy3L9KHpBZqpX6lTPzuQ9k4crBUeGsUVxgrTNyJMgTJSRnebx_6dbPa7uHEaZc5atM1wk74G_Qs9mhLu35MvJ4j2_qqZB2FiCfpxDHxxUuxyXonRiZS_AMY7Kgiauuyik3rLP3gAFJIbiwFo8lM62DNB_Ev2r5MUcSgrEBXcR5yjOiEYBjDV1LWxSlUiURUwPebtrPW87Yno7wACqWeGLA4KjfruWZMhXYnVEr0flk_UqwifixoOd5ebWh4Qrh7W8blTpDwXoDltyGW0ZUjxr3Rv_lSCeMFzXlZ_8qzC0d5JMoCbS0YpkwVY7UA";
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
