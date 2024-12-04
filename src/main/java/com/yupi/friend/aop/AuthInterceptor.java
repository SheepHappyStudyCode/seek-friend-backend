package com.yupi.friend.aop;

import com.yupi.friend.annotation.AuthCheck;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.enums.UserRoleEnum;
import com.yupi.friend.utils.JWTUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;



/**
 * 权限校验 AOP
 *
 * @author yupi
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 当前登录用户
        String token = request.getHeader("Authorization");
        Map<String, Object> userInfo = JWTUtils.verifyToken(token);

        Integer userRole = (Integer)userInfo.get("userRole");

        if(!mustRole.equals(UserRoleEnum.getEnumByValue(userRole).getText())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

