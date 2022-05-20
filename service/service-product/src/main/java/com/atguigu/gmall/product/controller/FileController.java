package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.minio.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

///admin/product/fileUpload
@Slf4j
@RestController
@RequestMapping("/admin/product")
public class FileController {
    @Autowired
    OSSService ossService;


    @PostMapping("/fileUpload")
    public Result uploadFile(MultipartFile file) throws Exception {
//        String fileName = file.getOriginalFilename();
//        long size = file.getSize();
//        String contentType = file.getContentType();
//        log.info("文件信息: 文件名:{}, 文件大小:{}, 文件类型:{}",fileName,size,contentType);

        String path = ossService.uploadFile(file);

        return Result.ok(path);
    }

}
