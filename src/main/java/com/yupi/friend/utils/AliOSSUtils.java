package com.yupi.friend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import static com.yupi.friend.config.ConstantPropertiesUtils.*;

public class AliOSSUtils {
    private static final String accessKeyId = ACCESS_KEY_ID;
    private static final String accessKeySecret = ACCESS_KEY_SECRET;
    private static final String endPoint = END_POINT;
    private static final String bucketName = BUCKET_NAME;

    public static String uploadFile(MultipartFile file){
        if(file.getSize() > 1024 * 1024){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件太大");
        }

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            ossClient.putObject(bucketName, fileName, inputStream);


            return ossClient.generatePresignedUrl(bucketName, fileName, new Date(System.currentTimeMillis() + 3600 * 1000)).toString();

        }

        catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }

    public static void deleteFile(String url){
        if(url == null || !url.contains(bucketName)){
            return;
        }

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);

        try {
            // https://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/968c6fd4-30d7-4061-abb3-0fec7f94ffc2_%E5%B0%8F%E6%B3%A2%E5%A5%87.jpg
            String fileName = url.split(endPoint + "/")[1];

            ossClient.deleteObject(bucketName, fileName);

        }

        catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }

}
