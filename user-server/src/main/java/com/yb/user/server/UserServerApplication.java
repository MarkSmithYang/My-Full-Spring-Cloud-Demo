package com.yb.user.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServerApplication {

    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    //注意这里设置的redis的序列化需要和网关使用的redisTemplate的序列化保持一致,
    //实测,如果存储和获取使用的redisTemplate的序列化方式不一致,将导致获取失败
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTemplate<String, Serializable> redisTemplate() {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(redisTemplate.getStringSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    public static void main(String[] args) {
        SpringApplication.run(UserServerApplication.class, args);
    }

    /**
     * 实例化加密类
     *
     * @return
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    class AuthenticationExample {
//
//        private static AuthenticationManager am = new SampleAuthenticationManager();
//
//        public static void main(String[] args) throws Exception {
//
//            String name = "";
//            String password = "";
//            try {
//                // request就是第一步，使用name和password封装成为的token
//                Authentication request = new UsernamePasswordAuthenticationToken(name, password);
//                // 将token传递给Authentication进行验证
//                Authentication result = am.authenticate(request);
//                SecurityContextHolder.getContext().setAuthentication(result);
//                break;
//            } catch (AuthenticationException e) {
//                System.out.println("认证失败：" + e.getMessage());
//            }
//            System.out.println("认证成功，Security context 包含：" + SecurityContextHolder.getContext().getAuthentication());
//        }
//    }
//
//    // 自定义验证方法
//    class SimpleAuthenticationManager implements AuthenticationManager {
//        static final List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>();
//
//        // 构建一个角色列表
//        static {
//            AUTHORITIES.add(new SimpleGrantedAuthority("ROLE_USER"));
//        }
//
//        // 验证方法
//        public Authentication authenticate(Authentication auth) throws AuthenticationException {
//            // 这里我们自定义了验证通过条件：username与password相同就可以通过认证
//            if (auth.getName().equals(auth.getCredentials())) {
//                return new UsernamePasswordAuthenticationToken(auth.getName(), auth.getCredentials(), AUTHORITIES);
//            }
//            // 没有通过认证则抛出密码错误异常
//            throw new BadCredentialsException("Bad Credentials");
//        }
//    }

}
