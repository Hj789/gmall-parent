package com.atguigu.gmall.product;


import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinioTest {

    @Test
    public void uploadTest(){
        try {
            // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
            MinioClient minioClient = new MinioClient("http://192.168.203.203:9000", "admin", "admin123456");

            // 检查存储桶是否已经存在
            boolean isExist = minioClient.bucketExists("gmall");
            if(isExist) {
                System.out.println("Bucket 已存在");
            } else {
                // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
                minioClient.makeBucket("gmall");
            }

            // 使用putObject上传一个文件到存储桶中。
            FileInputStream stream = new FileInputStream("C:\\Users\\87400\\Pictures\\Saved Pictures\\setmeal.jpg");
            //上传设置项
            PutObjectOptions options = new PutObjectOptions(stream.available(),-1);
            options.setContentType("image/png");
            minioClient.putObject("gmall","setmeal.jpg",stream,options);
            System.out.println("上传结束");
        } catch(MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Error occurred: " + e);
        }
    }
}
