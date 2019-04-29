package com.yb.gateway.server.filter;

import com.yb.common.server.dic.JwtDic;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtUtils;
import com.yb.common.server.utils.LoginUserUtils;
import com.yb.gateway.server.config.ApplicationPermitConfig;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Description: 实测这个Filter会在请求路由前
 * author biaoyang
 * date 2019/4/23 002317:40
 */
@Configuration
@AllArgsConstructor
public class GatewayServerFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(GatewayServerFilter.class);

    //通过构造的方式来注入
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final ApplicationPermitConfig permitConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst(JwtDic.HEADERS_NAME);
        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        // 解决中文静态资源拿不到的问题 以及 jwt放置在参数上的问题
        try {
            // 不解码其它服务的中文path
            if (!path.contains("-")) {
                path = URLDecoder.decode(path, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            log.warn("Path解码错误:", e);
        }
        final ServerHttpRequest httpRequest;
        if (StringUtils.isBlank(authorization)) {
            authorization = JwtDic.HEADERS_VALUE_PREFIX + request.getQueryParams().getFirst(JwtDic.HEADERS_NAME);
            if (StringUtils.isNotBlank(authorization)) {
                httpRequest = request
                        .mutate()
                        .path(path)
                        .header(JwtDic.HEADERS_NAME, authorization).build();
            } else {
                httpRequest = request
                        .mutate()
                        .path(path)
                        .build();
            }
        } else {
            httpRequest = request
                    .mutate()
                    .path(path)
                    .build();
        }
        exchange = exchange
                .mutate()
                .request(httpRequest)
                .build();
        //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
        //判断不需要登录的url
        //判断该uri是否是不需要登录的
        if (ArrayUtils.isNotEmpty(permitConfig.getPermitUrls())) {
            for (String pattern : permitConfig.getPermitUrls()) {
                if (pathMatcher.match(pattern, path)) {
                    //如果匹配上则返回
                    return chain.filter(exchange);
                }
            }
        }
        //验证token的合法性
        LoginUser loginUser = JwtUtils.checkAndGetPayload(authorization, JwtDic.BASE64_ENCODE_SECRET);
        if (Objects.nonNull(loginUser)) {
            //判断jti是否在redis里(主要是用这个联合判断jwt的token是否手动使其失效)
            if (StringUtils.isNotBlank(JwtDic.BASE64_ENCODE_SECRET)) {
                Boolean flag = redisTemplate.opsForSet().isMember(JwtDic.REDIS_SET_JTI_KEY + loginUser.getUsername(), loginUser.getJti());
                if (Objects.nonNull(flag) && flag) {
                    //设置登录用户信息到LoginUserUtils工具类中
                    LoginUserUtils.setUser(loginUser);
                    //注意这个方式没法解决接口权限认证的问题,如果加security的全局方法认证,需要引入security依赖,引入依赖就会导致url被拦截,又需要做许多事,
                    //而且不知道怎么传递那个登陆信息到其他系统去,因为需要通过共享登陆信息,才能通过登录信息去,认证接口的权限,要么就使用zuul的网关,要么就使用
                    //webflux整合security的配置
                    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                    //这个只能用在网关这个工程,无法传递过去给另一个项目,所以这个没有什么用,也就是其他被路由的工程是没法通过security
                    // 注解来完成接口方法的认证的,因为是两个项目,路由过去上下文的就丢失了
                    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                    //构造封装角色(权限)的集合
                    Set<GrantedAuthority> set = new HashSet<>();
                    //判断用户是否带有角色信息
                    if (!CollectionUtils.isEmpty(loginUser.getRoles())) {
                        //@PreAuthorize("hasRole('admin')"),这个看源码它会自动加前缀的,
                        //所以构造权限进去的时候也需要添加上相同的前缀,所以最好都去看下源码是啥前缀
                        loginUser.getRoles().forEach(s -> set.add(new SimpleGrantedAuthority("ROLE_" + s)));
                    }
                    //构造用户登录信息
                    Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), null, set);
                    //设置登录信息到上下文中
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                    return chain.filter(exchange);
                }
            } else {
                log.info("redis里找不到该用户登录的token的jti信息");
            }
        }
        //不合法请求,提示登录设置
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        DataBuffer wrap = exchange.getResponse().bufferFactory().wrap("please sign in".getBytes());
        return exchange.getResponse().writeWith(Mono.just(wrap));
    }

}
