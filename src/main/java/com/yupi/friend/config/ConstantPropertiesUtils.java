package com.yupi.friend.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
@Component
public class ConstantPropertiesUtils implements InitializingBean {
 
    //读取配置文件的内容
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.end-point}")
    private String endPoint;
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${jwt.token-secret}")
    private String tokenSecret;
    //定义公共静态常量

    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String END_POINT;
    public static String BUCKET_NAME;
    public static String TOKEN_SECRET;
    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT = endPoint;
        ACCESS_KEY_ID = accessKeyId;
        ACCESS_KEY_SECRET = accessKeySecret;
        BUCKET_NAME = bucketName;
        TOKEN_SECRET = tokenSecret;
    }
}