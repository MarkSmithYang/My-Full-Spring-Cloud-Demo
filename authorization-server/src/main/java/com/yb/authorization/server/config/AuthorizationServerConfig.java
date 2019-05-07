package com.yb.authorization.server.config;

import com.yb.authorization.server.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
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
//@EnableResourceServer
@EnableAuthorizationServer//提供/oauth/authorize,/oauth/token,/oauth/check_token,/oauth/confirm_access,/oauth/error
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

    /**
     * 1这里记得设置requestMatchers,不拦截需要token验证的url
     * --不然会优先被这个filter拦截,走用户端的认证而不是token认证
     * 2这里记得对oauth的url进行保护,正常是需要登录态才可以
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().and().httpBasic().disable()
                .requestMatchers().antMatchers("/", "/oauth/token/**", "/oauth/authorize/**").and()
                //需要放开认证服务器获取token,code的url(不知道为何security.tokenKeyAccess("permitAll()")--
                //放开token请求的url,但似乎没有生效,可能是因为继承的关系吧)
                .authorizeRequests().antMatchers("/oauth/**").permitAll();
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
        //对获取Token的请求不再拦截,设不设置,似乎没有什么关系
        //如果使用JWT令牌，则公开用于令牌验证的公钥
        security.tokenKeyAccess("permitAll()")
                //验证获取Token的验证信息--允许检查令牌
                .checkTokenAccess("isAuthenticated()")
                //允许提交表单,一般都是json对象提交的
                .allowFormAuthenticationForClients();
        //设置加密,否则不能解密密码
        security.passwordEncoder(bCryptPasswordEncoder());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //这个就类似于你在百度申请的client_id和client_secret等信息
        clients.inMemory()
                .withClient("android")
                .secret("$2a$10$rrt6qLPTs6Cj79GjgXy5/uTwKxifg65YQXiPJaFl/.YZvwVxjj2UW")
                .scopes("xx")
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                //这个为true表示不用登录用户手动授权,直接通过返回code(这个主要是authorization_code的情况),false就是用户点击授权才会得到code
                .autoApprove(false)
                //这个是必须要的,不然就会导致跳转uri的时候出现At least one redirect_uri must be registered with the client(必须至少向客户机注册一个redirect_uri)
                .redirectUris("https://www.baidu.com")
                .accessTokenValiditySeconds(600)
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
