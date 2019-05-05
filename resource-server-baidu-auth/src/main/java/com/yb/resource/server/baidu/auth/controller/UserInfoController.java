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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.MapUtils;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Description:
 * author biaoyang
 * date 2019/4/8 000819:21
 */
@Validated
@CrossOrigin
@Controller
public class UserInfoController {

    @Bean(name = "myRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 测试接口
     *
     * @return
     */
    @PreAuthorize("hasRole('guest')")
    @GetMapping("yes")
    @ResponseBody
    public String yes() {
        return "hello yes";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @GetMapping("no")
    @ResponseBody
    public String no() {
        return "hello no";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @PreAuthorize("hasRole('admin')")
    @GetMapping("world")
    @ResponseBody
    public String world() {
        return "hello world";
    }

    /**
     * 测试接口
     *
     * @return
     */
    @PreAuthorize("isAuthenticated()")//已验证合法性后可访问
    @GetMapping("hello")
    @ResponseBody
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
    @ResponseBody
    public Principal user(Principal user) {
        System.err.println("哎呦,不错哦");
        return user;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @ApiOperation("简易用户登录获取token")
    @GetMapping("/userLogin")
    @ResponseBody
    public JSONObject userLogin() {
        //先获取授权码,再获取token,然后通过token获取百度账号的用户基本信息
        String url = "https://openapi.baidu.com/rest/2.0/passport/users/getLoggedInUser?access_token=21.b2696fd2a747a40f713344b80bfee632.2592000.1559210210.1143166762-16078580";
        JSONObject jsonObject = restTemplate().getForObject(url, JSONObject.class);
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