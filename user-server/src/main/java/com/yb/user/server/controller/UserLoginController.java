package com.yb.user.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.yb.common.server.dic.JwtDic;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtUtils;
import com.yb.user.server.model.UserInfo;
import com.yb.user.server.repository.UserInfoRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description:
 * author biaoyang
 * date 2019/4/26 002611:43
 */
@Api(tags = "用户登录服务类")
@Validated
@CrossOrigin
@Controller
@AllArgsConstructor
public class UserLoginController {
    //通过构造注入
    private final RedisTemplate redisTemplate;
    private final UserInfoRepository userInfoRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @ApiOperation("异常捕获统一处理页接口")
    @GetMapping("/checkMessage")
    public String checkMessage() {
        return "check";
    }

    @ApiOperation("跳转登录页接口")
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @ApiOperation("跳转注册页接口")
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @ApiOperation("跳转注册页接口")
    @GetMapping("/findAll")
    @ResponseBody
    public List<UserInfo> findAll() {
        List<UserInfo> result = userInfoRepository.findAll();
        return result;
    }

    @ApiOperation("简易用户登录获取token")
    @GetMapping("userLogin")
    @ResponseBody
    public JSONObject userLogin(
            @ApiParam(value = "用户密码", required = true)
            @NotBlank(message = "用户密码不能为空")
            @Length(max = 16, min = 4, message = "用户名或密码错误")
            @RequestParam String password,

            @ApiParam(value = "用户名", required = true)
            @NotBlank(message = "用户名不能为空")
            @Length(max = 10, message = "用户名或密码错误")
            @RequestParam String username) {
        //初始化JSONObject对象
        JSONObject jsonObject = new JSONObject();
        //查询用户名是否存在
        UserInfo userInfo = userInfoRepository.findByUsername(username);
        //判断并校验密码
        if (userInfo != null && bCryptPasswordEncoder.matches(password, userInfo.getPassword())) {
            //封装信息到LoginUser
            LoginUser loginUser = new LoginUser();
            loginUser.setUsername(userInfo.getUsername());
            loginUser.setOrgName("搞笑部");
            loginUser.setJti(JwtUtils.createJti());
            loginUser.setRoles(new HashSet<>(Arrays.asList(userInfo.getRoles())));
            //用户的登录信息正确,为用户生成token,秘钥和gateway-server保持一致
            String accessToken = JwtUtils.createAccessToken(loginUser, 10 * 60 * 1000, JwtDic.BASE64_ENCODE_SECRET);
            String refreshToken = JwtUtils.createRefreshToken(userInfo.getUsername(), 60 * 60 * 1000, JwtDic.BASE64_ENCODE_SECRET);
            //将jwt的唯一标志存储在redis上--->set没有设置某元素过时间时间的功能,据说默认时间是30天
            redisTemplate.opsForSet().add(JwtDic.REDIS_SET_JTI_KEY + loginUser.getUsername(), loginUser.getJti());
            Set<GrantedAuthority> authorities = new HashSet<>(5);
            //设置登录信息到上下文
            if (!CollectionUtils.isEmpty(loginUser.getRoles())) {
                loginUser.getRoles().forEach(s->authorities.add(new SimpleGrantedAuthority(s)));
            }
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginUser.getUsername(),authorities));
            //封装token
            jsonObject.put("accessToken", accessToken);
            jsonObject.put("refreshToken", refreshToken);
        }
        //返回数据
        return jsonObject;
    }

    @ApiOperation("简易用户注册")
    @GetMapping("userRegister")
    @Transactional(rollbackFor = Exception.class)
    public String userRegister(
            @ApiParam(value = "用户密码", required = true)
            @NotBlank(message = "用户密码不能为空")
            @Length(max = 16, min = 4, message = "用户密码长度为4~16为")
            @RequestParam String password,

            @ApiParam(value = "用户角色")
            @RequestParam(required = false) String[] roles,

            @ApiParam(value = "用户名", required = true)
            @NotBlank(message = "用户名不能为空")
            @Length(max = 10, message = "用户名过长")
            @RequestParam String username, Model model) {
        //判断该用户是都已经存在
        if (userInfoRepository.findByUsername(username) != null) {
            model.addAttribute("error", "该用户名已经被注册了");
            return "register";
        }
        //封装用户数据
        UserInfo userInfo = new UserInfo();
        //因为用的是mongodb,所以可以不用设置id
        userInfo.setUsername(username);
        userInfo.setPassword(bCryptPasswordEncoder.encode(password));
        userInfo.setRoles(roles);
        //添加用户数据
        userInfoRepository.save(userInfo);
        model.addAttribute("message", "注册成功");
        return "login";
    }
}
