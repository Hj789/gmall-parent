package com.atguigu.gmall.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.model.vo.user.LoginUserResponseVo;
import com.atguigu.gmall.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo>
    implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public LoginUserResponseVo login(UserInfo userInfo) {

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("login_name",userInfo.getLoginName());
        wrapper.eq("passwd",MD5.encrypt(userInfo.getPasswd()));
        UserInfo loginUser = userInfoMapper.selectOne(wrapper);
        if (loginUser == null){
            //前端带的账号密码是错误的
            return null;
        }
        //用户登录成功
        LoginUserResponseVo responseVo = new LoginUserResponseVo();
        //去redis保存登录成功的用户信息
        loginUser.setIpAddr(userInfo.getIpAddr()); //Ip复制过来
        String token = saveUserAuthenticationInfo(loginUser);
        //设置令牌
        responseVo.setToken(token);
        //设置昵称
        responseVo.setNickName(loginUser.getNickName());

        return responseVo;
    }

    @Override
    public String saveUserAuthenticationInfo(UserInfo userInfo) {
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        //user:login:token = {用户数据}
        //redis按照token与用户的对应关系保存令牌
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_PREFIX+token,JSONs.toStr(userInfo),7, TimeUnit.DAYS);

        return token;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(RedisConst.USER_LOGIN_PREFIX+token);
    }
}



