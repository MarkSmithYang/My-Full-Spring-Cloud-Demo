package com.yb.resource.server.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

/**
 * Description:
 * author biaoyang
 * date 2019/4/8 000819:21
 */
@Validated
@CrossOrigin
@RestController
public class ResourceServerController {

    /**
     * 测试接口
     *
     * @return
     */
    @PreAuthorize("hasRole('guest')")//已验证合法性后可访问
    @GetMapping("hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 获取授权用户信息
     *
     * @param user 当前用户
     * @return 授权信息
     */
    @GetMapping("/user")
    public Principal user(Principal user) {
        System.err.println("哎呦,不错哦");
        return user;
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @GetMapping("/index")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

}