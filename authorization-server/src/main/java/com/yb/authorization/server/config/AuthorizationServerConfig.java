package com.yb.authorization.server.config;

import com.yb.authorization.server.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * Description: 一般来讲,认证服务器是第三方提供的服务,比如你想接入qq登陆接口,那么认证服务器就是腾讯提供,,
 * 然后你在本地做资源服务,但是认证和资源服务不是非要物理上的分离,只需要做到逻辑上的分离就好
 * 继承WebSecurityConfigurerAdapter的目的这里仅仅只是为了获取AuthenticationManager的bean,当然了如果写
 * 了另一个配置来继承AuthenticationManager也是需要这个bean的,我这里是觉得没必要再写了
 * --------------这个是类似与第三方的的认证服务(可替代第三方完成认证功能)
 * author biaoyang
 * Date: 2019/4/18 0018
 */
@Configuration
@EnableResourceServer
@EnableAuthorizationServer
@EnableGlobalMethodSecurity(prePostEnabled = true)//开启全局方安全认证
public class AuthorizationServerConfig extends WebSecurityConfigurerAdapter implements AuthorizationServerConfigurer {

    @Autowired
    public UserDetailsServiceImpl userDetailsService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

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

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.userDetailsService(userDetailsService)
                .authenticationManager(authenticationManagerBean());
        endpoints.tokenStore(tokenStore());
        //实测这个对于jwt来说是必要的,不管不增强不增强,都需要设置这个才能正确获取到jwt的token,
        //否则只是个UUID,是没实现jwt存储和转换的,这里仅仅只是使用了,普通的字符串作为秘钥签名的
        //可以使用公私钥加密和增强(添加额外的内容到jwt里)来
        endpoints.tokenEnhancer(jwtAccessTokenConverter());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //对获取Token的请求不再拦截,舍不设置,似乎没有什么关系
        security.tokenKeyAccess("permitAll()")
                //验证获取Token的验证信息
                .checkTokenAccess("isAuthenticated()")
                //允许提交表单,一般都是json对象提交的
                .allowFormAuthenticationForClients();
        //设置加密,否则不能解密密码
        security.passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("android")
                .secret("$2a$10$rrt6qLPTs6Cj79GjgXy5/uTwKxifg65YQXiPJaFl/.YZvwVxjj2UW")
                .scopes("xx")
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .autoApprove(false)
                .redirectUris("https://www.baidu.com")
                .and()
                .withClient("web")
                .secret("$2a$10$fhHcKTMUT3Y1XPfrCYRDAeSRFhOiRH0gzndNjkcgg1iAlPRQw7fDq")
                .scopes("yy")
                //简易模式
                .authorizedGrantTypes("implicit")
                //设置token的过期时间,这个是这client的token的时间
                .accessTokenValiditySeconds(300);
    }

}
