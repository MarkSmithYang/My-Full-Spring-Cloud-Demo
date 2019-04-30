package com.yb.resource.server.baidu.auth.config;

import com.yb.resource.server.baidu.auth.filter.MyServerFilter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.client.RestTemplate;

/**
 * Description: 资源服务器
 * author biaoyang
 * date 2019/4/22
 */
@Configuration
@EnableWebSecurity
@EnableResourceServer
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ResourceServerConfig.class);

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
                .antMatchers("/", "/login").permitAll()
                .anyRequest()
                .authenticated();
    }
}
