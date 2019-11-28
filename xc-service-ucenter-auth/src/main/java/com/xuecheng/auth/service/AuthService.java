package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.ErrorHandler;
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
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${auth.tokenValiditySeconds}")
    private long tokenValiditySeconds;


    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        String jsonString = JSON.toJSONString(authToken);
        //将令牌存储到redis
        boolean saveToken = saveToken(authToken.getAccess_token(), jsonString, tokenValiditySeconds);
        if(!saveToken){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }

        return authToken;
    }

    //保存token到redis中
    private boolean saveToken(String accress_token,String content,long ttl){

        String key = "user_token:"+accress_token;
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(key,TimeUnit.SECONDS);
        return expire>0;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){

        //获取授权中心地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        //封装请求地址
        String url = uri+"/auth/oauth/token";

        //封装请求头
        HttpHeaders headers = new HttpHeaders();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        headers.add("Authorization",httpBasic);

        //封装请求体
        LinkedMultiValueMap map = new LinkedMultiValueMap();
        map.add("grant_type","password");
        map.add("password",password);
        map.add("username",username);

        HttpEntity<MultiValueMap> httpEntity = new HttpEntity<>(map,headers);
        //设置错误处理
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //如果返回400 或 401 不做处理继续执行
                if(response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        //调用远程请求
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        Map body = exchange.getBody();
        if(body == null ||
                body.get("access_token") == null ||
                body.get("refresh_token") == null ||
                body.get("jti") == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) body.get("jti"));
        authToken.setRefresh_token((String) body.get("refresh_token"));
        authToken.setJwt_token((String) body.get("access_token"));
        return authToken;
    }

    //将令牌保存到redis


    //获取HttpBasic
    public String getHttpBasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

}
