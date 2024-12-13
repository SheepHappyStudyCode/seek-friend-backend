package com.yupi.friend.aop;

import com.yupi.friend.annotation.AuthCheck;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.interceptor.UserHolder;
import com.yupi.friend.model.enums.UserRoleEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


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

        // 当前登录用户
        if(UserHolder.getUser() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        int userRole = UserHolder.getUser().getUserRole();

        if(!mustRole.equals(UserRoleEnum.getEnumByValue(userRole).getText())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

