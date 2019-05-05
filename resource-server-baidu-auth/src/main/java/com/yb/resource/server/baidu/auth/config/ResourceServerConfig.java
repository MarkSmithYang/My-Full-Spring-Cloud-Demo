package com.yb.resource.server.baidu.auth.config;

import com.yb.resource.server.baidu.auth.filter.MyServerFilter;
import lombok.AllArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

/**
 * Description: 资源服务器-------------------->实测资源服务器需要和认证服务器配套使用,因为它开启了注解,
 * 就开启了它默认的过滤器去tokenStore找token,自然就找不到了,所以弄成一个普通的有security保护的项目,然后
 * 通过去第三方应用例如百度,按以前的方式去获取token(去掉了OAuth2,不知道还行不行,没测试)用户信息,生成token给自己的项目用
 * author biaoyang
 * date 2019/4/22
 */
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    private final MyServerFilter myServerFilter;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().and().httpBasic().disable()
                //这个设置是必要的,具体原因不细说了,在user-server有详细说
                .addFilterAfter(myServerFilter, SecurityContextPersistenceFilter.class)
                //资源服务设置的这个登录才是比较好的,因为会先来访问它,总之,先访问谁,谁就需要抛出登录提示
                .exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
            //当访问接口时,没有登录不再提示请登录,而是直接跳转到登录页去
            response.sendRedirect("/login");
            //设置响应编码,避免中文乱码,并设置提示响应---->这个相对来说并不太友好,可设置直接跳转到登录也(如上),不能同时存在这两种方式
            //response.setCharacterEncoding("UTF-8");
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请登录");
        }).and()
                .authorizeRequests()
                .antMatchers("/", "/login", "/userLogin").permitAll()
                .anyRequest()
                .authenticated();
    }
}
