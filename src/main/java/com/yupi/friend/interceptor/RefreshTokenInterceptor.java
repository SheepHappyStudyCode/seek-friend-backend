package com.yupi.friend.interceptor;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yupi.friend.constant.RedisConstant.USER_LOGIN_KEY;
import static com.yupi.friend.constant.RedisConstant.USER_LOGIN_TTL;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");

        if(StringUtils.isEmpty(token)){
            return true;
        }

        String key = USER_LOGIN_KEY + token;
        Map<String, Object> userInfo = (Map) redisTemplate.opsForValue().get(key);
        if(userInfo != null){
            redisTemplate.expire(key, USER_LOGIN_TTL, TimeUnit.MINUTES);
            UserDTO userDTO = BeanUtil.mapToBean(userInfo, UserDTO.class, false);
            UserHolder.saveUser(userDTO);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
