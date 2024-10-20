package com.yupi.friend.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.yupi.friend.utils.JWTUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");

        if(StringUtils.isEmpty(token)){
            return true;
        }
        // todo 刷新 token 有效期

        Map<String, Object> userInfo = JWTUtils.verifyToken(token);
        if(userInfo != null){
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
