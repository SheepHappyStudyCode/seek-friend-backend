package com.yupi.friend.config;

import com.yupi.friend.interceptor.LoginInterceptor;
import com.yupi.friend.interceptor.RefreshTokenInterceptor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
@ConfigurationProperties(prefix = "cross-origin")
@Data
public class WebMvcConfig implements WebMvcConfigurer {

    private String allowOrigin;

    @Resource
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当 **Credentials为true时，** Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins(allowOrigin)
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // knife4j 路由
        String[] excludePatterns = new String[]{"/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**",
                "/api", "/api-docs", "/api-docs/**", "/doc.html/**"};


        registry.addInterceptor(refreshTokenInterceptor);
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/user/search",
                        "/user/recommend",
                        "/user/team/members",
                        "/team",
                        "/team/list/**",
                        "/post/query",
                        "/comment/query",
                        "/commentAnswer/query"
                )
                .excludePathPatterns(excludePatterns);
    }
}
