package com.atguigu.gmall.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录业务处理
 */
@Controller
public class LoginController {


    //http://passport.gmall.com/login.html?originUrl=http://gmall.com/

    /**
     * 展示登录页面
     * @param originUrl 登录成功以后调回哪里
     * @return
     */
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("originUrl") String originUrl, Model model){

        model.addAttribute("originUrl",originUrl);


        return "login";
    }
}
