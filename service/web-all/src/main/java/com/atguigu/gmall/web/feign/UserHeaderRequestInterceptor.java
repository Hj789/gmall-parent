package com.atguigu.gmall.web.feign;

import com.atguigu.gmall.web.controller.CartController;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class UserHeaderRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {

//        HttpServletRequest httpServletRequest = CartController.threadLocal.get();
        //1.得到当前请求(请求刚进来 Tomcat接到的这个老请求)
        ServletRequestAttributes request = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String userId = request.getRequest().getHeader("UserId");
        String userTempId = request.getRequest().getHeader("UserTempId");
        if (userId != null){
            template.header("UserId",userId);
        }
        if (userTempId != null){
            template.header("UserTempId",userTempId);
        }
    }
}
