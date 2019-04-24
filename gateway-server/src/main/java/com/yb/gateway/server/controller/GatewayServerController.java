package com.yb.gateway.server.controller;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Description: 网关服务控制层
 * author biaoyang
 * date 2019/4/23 002310:00
 */
@Validated
@CrossOrigin
@RestController
public class GatewayServerController {

    @GetMapping("/test")
    public String test() {
        return "hello world";
    }

    @GetMapping("/fallback")
    public Mono<String> fallback() {
        Mono<String> mono = Mono.just("我熔断了哦");
        return mono;
    }

}
