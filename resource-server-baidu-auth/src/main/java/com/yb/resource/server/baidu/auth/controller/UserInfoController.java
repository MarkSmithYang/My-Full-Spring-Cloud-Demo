package com.yb.resource.server.baidu.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

/**
 * Description:
 * author biaoyang
 * date 2019/4/8 000819:21
 */
@Validated
@CrossOrigin
@Controller
public class UserInfoController {

    /**
     * 测试接口
     * @return
     */
    @PreAuthorize("hasRole('guest')")
    @GetMapping("yes")
    @ResponseBody
    public String yes(){
        return "hello yes";
    }

    /**
     * 测试接口
     * @return
     */
    @GetMapping("no")
    @ResponseBody
    public String no(){
        return "hello no";
    }

    /**
     * 测试接口
     * @return
     */
    @PreAuthorize("hasRole('admin')")
    @GetMapping("world")
    @ResponseBody
    public String world(){
        return "hello world";
    }

    /**
     * 测试接口
     * @return
     */
    @PreAuthorize("isAuthenticated()")//已验证合法性后可访问
    @GetMapping("hello")
    @ResponseBody
    public String hello(){
        return "hello world";
    }

    /**
     * 获取授权用户信息
     *
     * @param user 当前用户
     * @return 授权信息
     */
    @GetMapping("/user")
    @ResponseBody
    public Principal user(Principal user) {
        System.err.println("哎呦,不错哦");
        return user;
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

}
