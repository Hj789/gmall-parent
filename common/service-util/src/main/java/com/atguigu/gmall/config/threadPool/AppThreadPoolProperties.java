package com.atguigu.gmall.config.threadPool;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "app.threadpool")
public class AppThreadPoolProperties {


    private Integer corePoolSize = 4;
    private Integer maximumPoolSize = 4;
    private Long keepAliveTime = 5L; //以分钟为单位
    private TimeUnit unit = TimeUnit.MINUTES;
    private Integer queueSize = 1000;

    //如果没有异步能力，慢一点就慢一点，同步把他运行完
    private RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor
            .CallerRunsPolicy();

}
