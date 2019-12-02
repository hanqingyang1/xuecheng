package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginFilter extends ZuulFilter{

    @Autowired
    private AuthService authService;


    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //获取requestContext
        RequestContext requestContext = RequestContext.getCurrentContext();
        //获取Request
        HttpServletRequest request = requestContext.getRequest();
        //获取Response
        HttpServletResponse response = requestContext.getResponse();

        //从Cookie中获取accress_token
        String access_token = authService.getTokenFromCookie(request);
        if(StringUtils.isBlank(access_token)){
            access_denied();
        }

        //从Header中获取jwtToken
        String jwt = authService.getJwtFromHeader(request);
        if(StringUtils.isBlank(jwt)){
            access_denied();
        }

        //检验令牌有效期
        long expire = authService.getExpire(access_token);
        if(expire < 0){
            access_denied();
        }

        return null;
    }

    //拒绝访问
    private void access_denied(){
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        requestContext.setSendZuulResponse(false);//拒绝访问
        //设置响应内容
        ResponseResult responseResult =new ResponseResult(CommonCode.UNAUTHENTICATED);
        String responseResultString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(responseResultString);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        HttpServletResponse response = requestContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }
}
