package com.yb.resource.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * Description: 资源服务器
 * author biaoyang
 * date 2019/4/17 001719:07
 */
@Configuration
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    //tokenStore和jwtAccessTokenConverter实测都是必须的,并且需要和AuthorizationServer那边的保持一致,还要秘钥要一致

    /**
     * token的存储方式,一般来说现在都是使用哦jwt的方式
     * 还有就是认证服务和资源服务都可以设置tokenStore
     *
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        //通过jwt来存储token信息,而不是redis
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        //实例化jwt的访问令牌转换对象
        JwtAccessTokenConverter tokenConverter = new JwtAccessTokenConverter();
        //不使用对称加密(也就是不使用公私钥加密解密的方式)---------其实加密解密都是自动完成的,也没有必要弄太复杂了
        tokenConverter.setSigningKey("MySecret123456789");
        //返回jwt的访问令牌转换对象
        return tokenConverter;
    }
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().and().httpBasic().disable()
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
