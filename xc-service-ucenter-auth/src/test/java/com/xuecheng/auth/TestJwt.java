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
import sun.security.rsa.RSAKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    @Test
    public void testCreateJwt(){
        //秘钥文件名
        String keystore = "xc.keystore";
        //秘钥库密码
        String keystore_password = "xuechengkeystore";
        //秘钥访问密码
        String keypass = "xuecheng";
        //秘钥文件别名
        String alias = "xckey";

        //获取秘钥文件
        ClassPathResource classPathResource = new ClassPathResource(keystore);

        //创建秘钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource,keystore_password.toCharArray());
        //获取秘钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keypass.toCharArray());
        //从秘钥对中获取私钥，并强转为RSAPrivateKey
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();

        //封装令牌内容
        Map<String,String> body = new HashMap<>();

        body.put("name","hanqingyang");

        String jsonString = JSON.toJSONString(body);
        //生成令牌
        Jwt jwt = JwtHelper.encode(jsonString, new RsaSigner(aPrivate));
        String encoded = jwt.getEncoded();
        System.out.println(encoded);

        Jwt jwt1 = JwtHelper.decode(encoded);
        String claims = jwt1.getClaims();
        System.out.println(claims);

    }

    @Test
    public void verify(){

        //获取公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        //封装token
        String token ="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaGFucWluZ3lhbmcifQ.eeFoOHq8j2I4jfmcTbtI3E7z4JZ_5JqwRNDrlnUHyBEEUP2fnu4AYukuDo23PvMi0QP3KCyQQ4NA9xEiqOlqD6njGLhzwINBKxI7qN5l-Pna5dqIzbWpBlaxioK7Tp2FAqwZI8a4uU4HBgUNGQieHahJJK4vDclUP4izfSw0yp-de3oEUbPmXnMp_ZdUuqTx-nY-usK7AORmK9ait-l3S-wabiqQsBXyu9knw7U86JD2fwi5kPaJzQ7kagwKRjRU7u8QUyDPUW1qIbxGmbRo0QGx7sHImK75Z-Jqt3MrpPwI3BPXVN0AjOyHQUebWmsOWdc-ZqD-ACl0XeYJiOZ7qw";
        //检验令牌
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        //获取令牌内容
        String claims = jwt.getClaims();

        System.out.println(claims);
    }
}
