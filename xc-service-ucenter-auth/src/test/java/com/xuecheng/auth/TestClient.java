package com.xuecheng.auth;


import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
    LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;


    @Test
    public void testCilent(){
        //从注册中心获取服务地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //获取auth服务地址
        URI uri = serviceInstance.getUri();

        String url = uri+"/auth/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        String httpBasic = getHttpBasic("XcWebApp", "XcWebApp");
        headers.add("Authorization",httpBasic);

        LinkedMultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("grant_type","password");
        map.add("password","123");
        map.add("username","itcast");
        //创建远程调用请求体
        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<>(map,headers);

        //设置报400和401正常返回
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!= 401) {
                    super.handleError(response);
                }
            }
        });
        //执行远程调用
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        Map body = exchange.getBody();

        System.out.println(body);


    }

    private String getHttpBasic(String clientId,String client_secret){
        String basicStr = clientId+":"+client_secret;
        byte[] encode = Base64Utils.encode(basicStr.getBytes());
        return "Basic " +new String(encode);
    }
}
