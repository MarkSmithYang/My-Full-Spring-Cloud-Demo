package com.yb.gateway.server.filter;

import com.yb.gateway.server.config.ApplicationPermitConfig;
import com.yb.gateway.server.other.JwtUtils;
import com.yb.gateway.server.other.LoginUser;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Objects;

/**
 * Description: 实测这个Filter会在请求路由前
 * author biaoyang
 * date 2019/4/23 002317:40
 */
@Configuration
@AllArgsConstructor
public class GatewayServerFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(GatewayServerFilter.class);

    public static final String REDIS_SET_JTI_KEY = "myJti";
    public static final String HEADERS_NAME = "Authorization";
    public static final String HEADERS_VALUE_PREFIX = "Bearer ";
    //通过构造的方式来注入
    private final RedisTemplate<String, Serializable> redisTemplate;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final ApplicationPermitConfig permitConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HEADERS_NAME);
        System.err.println("哈哈哈==" + authorization);
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
            authorization = HEADERS_VALUE_PREFIX + request.getQueryParams().getFirst(HEADERS_NAME);
            if (StringUtils.isNotBlank(authorization)) {
                httpRequest = request
                        .mutate()
                        .path(path)
                        .header(HEADERS_NAME, authorization).build();
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
        //判断jti是否在redis里(主要是用这个联合判断jwt的token是否手动使其失效)
        if (StringUtils.isNotBlank(permitConfig.getBase64Secret())) {
            LoginUser loginUser = JwtUtils.checkAndGetPayload(authorization, permitConfig.getBase64Secret());
            if (Objects.nonNull(loginUser)) {
                Boolean login = redisTemplate.opsForSet().isMember(REDIS_SET_JTI_KEY + loginUser.getUsername(), loginUser.getJti());
                if (Objects.nonNull(login) && login) {
                    chain.filter(exchange);
                }
            }
        }
        //不合法请求,提示登录设置
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        DataBuffer wrap = exchange.getResponse().bufferFactory().wrap("please sign in".getBytes());
        return exchange.getResponse().writeWith(Mono.just(wrap));
    }

}
