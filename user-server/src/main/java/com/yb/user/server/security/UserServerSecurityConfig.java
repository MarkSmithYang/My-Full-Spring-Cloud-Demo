package com.yb.user.server.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

/**
 * Description:
 * author biaoyang
 * date 2019/4/30 003010:35
 */
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)//开启全局方法认证
public class UserServerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyServerFilter userServerFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().and().httpBasic().disable()
                //实测,不需要是自定义类实现UserDetailsService和AuthenticationProvider来进行验证再设置安全上下文,直接构造生成Authentication进行set即可
                //而且实测必须在这里设置自定义的过滤器,否则不会走过滤器的
                //这里如果写自己实现或继承的过滤器,需要设置在其后面(因该是需要先加载过滤器信息,才能后信息给你继承或这个实现,否则异常)
                .addFilterAfter(userServerFilter, SecurityContextPersistenceFilter.class)
                .authorizeRequests().antMatchers("/","/login","/userLogin","/register").permitAll()
                //需要开启认证,来进行对方法认证,实测不设置这个也会拦截(抛出无权限访问),最好设置上吧,可读性强些
                .anyRequest().authenticated();
    }

}
