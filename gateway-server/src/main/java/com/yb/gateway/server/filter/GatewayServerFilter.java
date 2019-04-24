package com.yb.gateway.server.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Description: 实测这个Filter会在请求路由前
 * author biaoyang
 * date 2019/4/23 002317:40
 */
@Configuration
public class GatewayServerFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            System.err.println("哈哈哈==" + authorization);
        }
        //无法完成过滤请求的功能------待研究
        return chain.filter(exchange);
    }
}
