package com.yb.authorization.server.impl;

import com.alibaba.fastjson.JSONObject;
import com.yb.authorization.server.model.UserInfo;
import com.yb.authorization.server.repository.UserInfoRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:用户信息服务--实现 Spring Security的UserDetailsService接口方法,用于身份认证
 * author biaoyang
 * date 2019/4/8 000819:56
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("UserDetailsServiceImpl==========loadUserByUsername(String username)");
        //通过用户名获取用户信息--(默认用户名唯一)
        UserInfo userInfo = userInfoRepository.findByUsername(username);
        //判断用户是否存在
        if (userInfo != null) {
            //构造security的user返回
            return new User(userInfo.getUsername(), userInfo.getPassword(), userInfo.getAuthorities());
        } else {
            //抛出异常中断程序
            throw new UsernameNotFoundException("用户名或密码错误");
        }
    }

}
