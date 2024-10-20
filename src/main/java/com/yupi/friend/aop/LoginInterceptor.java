package com.yupi.friend.aop;

import com.yupi.friend.annotation.LoginCheck;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.utils.JWTUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * 登录校验 AOP
 */
//@Aspect
//@Component
public class LoginInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @return
     */
    @Around("@annotation(loginCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, LoginCheck loginCheck) throws Throwable {

        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 当前登录用户
        String token = request.getHeader("Authorization");
        Map<String, Object> userInfo = JWTUtils.verifyToken(token);

        if(userInfo == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

