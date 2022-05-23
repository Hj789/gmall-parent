package com.atguigu.gmall.product;

import com.atguigu.gmall.minio.config.MinioAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.context.annotation.Import;

/**
 * Mybatis-Plus 场景
 * 1、自动配置：扫描主类所在包下的所有@Mapper注解标注的组件。作为Dao组件，注入到Spring容器
 * 2、@MapperScan(basePackages = "com.atguigu.gmall.product.dao")
 *      指定包下的所有接口，都是mapper，请自动批量扫描
 *
 * 为什么service-util写的配置类，service-product微服务用不到？虽然依赖
 * 1、SpringBoot自动配置原理？
 *   扫描ProductApplication所在的包 "com.atguigu.gmall.product"
 *   工具类的包                     "com.atguigu.gmall.config"
 *
 *
 * SpringBoot：
 * 1、redis自动配置为例
 *
 *
 * 抽取了Minio
 *    Minio: com.atguigu.gmall.minio
 *    主类:   com.atguigu.gmall.product
 *
 * 未来每个微服务
 * 1、正常编写业务逻辑，如果有任何业务异常，需要自行throw new GmallException(业务的状态码)
 *    注意： 200是成功，剩下都是各种错误
 * 2、全局异常进行捕获处理。前端返回 json 数据。前端根据返回的业务状态码，决定显示的页面效果
 * 3、系统异常。 RuntimeException，  OOM
 *    全局异常进行捕获处理：
 *    OOM: java -jar -XX:+HeapDumpOnOutOfMemoryError xxx.jar
 *
 *
 * 整合redis
 * 1、引入redis-starter依赖
 * 2、配置redis的连接地址等
 * 3、springboot开启redis的自动配置
 *      RedisAutoConfiguration：
 *      1、RedisTemplate<Object, Object>
 *      2、StringRedisTemplate: RedisTemplate<String, String>  用它存数据，自已定义序列化方式，
 */


@SpringCloudApplication
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class,args);
    }
}
