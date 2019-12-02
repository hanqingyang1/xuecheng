package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //取出头信息
    public String getJwtFromHeader(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(StringUtils.isBlank(header)){
            return null;
        }
        if(!header.startsWith("Bearer ")){
            return null;
        }
        String jwt = header.substring(7);
        return jwt;
    }

    //从cookie中取出token
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if(!CollectionUtils.isEmpty(map)){
            String access_token = map.get("uid");
            if(StringUtils.isBlank(access_token)){
                return null;
            }
            return access_token;
        }
        return null;
    }

    /**
     * 查询令牌有效期
     * @return
     */
    public long getExpire(String  access_token){
        String key = "user_token:"+access_token;
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }

}
