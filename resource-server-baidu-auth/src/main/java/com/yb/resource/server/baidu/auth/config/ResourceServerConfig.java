package com.yb.resource.server.baidu.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.client.RestTemplate;

/**
 * Description: 资源服务器
 * author biaoyang
 * date 2019/4/22
 */
@Configuration
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ResourceServerConfig.class);

    @Bean(name = "myRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().and().httpBasic().disable()
//                .requestMatcher(request -> {
//                    String authorization = "21.f26ff6638ed4b4e4e584a263bb5f703c.2592000.1558520672.1143166762-16078580";
//                    try {
//                        JSONObject forObject = restTemplate().getForObject("https://openapi.baidu.com/rest/2.0/passport" +
//                                "/users/getLoggedInUser?access_token=" + authorization, JSONObject.class);
//                        if (!MapUtils.isEmpty(forObject)) {
//                            Long uid = forObject.getLong("uid");
//                            if (uid != null) {
//                                SecurityContext context = SecurityContextHolder.getContext();
//                                Authentication token = new UsernamePasswordAuthenticationToken(forObject.getString("uname"), null, null);
//                                context.setAuthentication(token);
//                                return true;
//                            }
//                        }
//                    } catch (RestClientException e) {
//                        log.info("无效的token信息", e);
//                    }
//                    return false;
//                })
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
