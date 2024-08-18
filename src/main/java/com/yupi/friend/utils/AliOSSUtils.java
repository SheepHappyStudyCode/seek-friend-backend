package com.yupi.friend.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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

            //
            https://web-friend-sheephappy.oss-cn-hangzhou.aliyuncs.com/1d9a3540-82c3-4832-bf0d-08cfa24435e2_20221114215948_1cf96.thumb.400_0.jpeg
            return String.format("https://%s.%s/%s", bucketName, endPoint, fileName);

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
