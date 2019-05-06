package com.yb.resource.server.baidu.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.yb.common.server.dic.JwtDic;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.MapUtils;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Description: 目前的swagger配置老是不对,没有使用
 * author biaoyang
 * date 2019/4/8 000819:21
 */
@Validated
@CrossOrigin
@RestController
public class MyResourceController {
    private static final String BAIDU_USER_URL = "https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser?access_token=";
    private static final String BAIDU_AUTHORIZE_URL = "https://openapi.baidu.com/oauth/2.0/authorize?response_type=code&client_id=&redirect_uri=oob";
    private static final String BAIDU_TOKNE_URL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=authorization_code&code=&client_id=&client_secret=&redirect_uri=oob";

    @Bean(name = "myRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 测试接口
     *
     * @return
     */
    @ApiOperation("测试接口yes")
    @PreAuthorize("hasRole('guest')")
    @GetMapping("yes")
    public String yes() {
        return "hello yes";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @ApiOperation("测试接口no")
    @GetMapping("no")
    public String no() {
        return "hello no";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @ApiOperation("测试接口world")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("world")
    public String world() {
        return "hello world";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @ApiOperation("测试接口hello")
    @PreAuthorize("isAuthenticated()")//已验证合法性后可访问
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
    @ApiOperation("获取当前用户信息")
    @GetMapping("/user")
    public Principal user(@ApiParam(value = "当前用户信息,直接用参数接收的") Principal user) {
        return user;
    }

    @ApiOperation("登录跳转页")
    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("login");
    }

    @ApiOperation("简易用户登录获取token")
    @GetMapping("/userLogin")
    public JSONObject userLogin(
            @ApiParam(value = "访问百度用户信息的token")
            @NotBlank(message = "令牌不能为空")
            @Length(max = 200, message = "令牌有误")
            @RequestParam String baiDuAccessToken) {
        //先获取授权码,再获取token,然后通过token获取百度账号的用户基本信息,直接占位,不用判断是否为空
        String url = BAIDU_USER_URL+"{baiDuAccessToken}";
        JSONObject jsonObject = restTemplate().getForObject(url, JSONObject.class, baiDuAccessToken);
        //判断并处理数据
        if (!MapUtils.isEmpty(jsonObject)) {
            //封装信息到LoginUser
            LoginUser loginUser = new LoginUser();
            loginUser.setUsername(jsonObject.getString("uname"));
            loginUser.setOrgName("搞笑部");
            loginUser.setJti(JwtUtils.createJti());
            Set<String> set = new HashSet<>(5);
            set.add("admin");
            set.add("boss");
            loginUser.setRoles(set);
            //用户的登录信息正确,为用户生成token,秘钥和gateway-server保持一致
            String accessToken = JwtUtils.createAccessToken(loginUser, 30 * 60 * 1000, JwtDic.BASE64_ENCODE_SECRET);
            String refreshToken = JwtUtils.createRefreshToken(jsonObject.getString("uname"), 60 * 60 * 1000, JwtDic.BASE64_ENCODE_SECRET);
            //封装token
            jsonObject.put("accessToken", accessToken);
            jsonObject.put("refreshToken", refreshToken);
        }
        return jsonObject;
    }
}