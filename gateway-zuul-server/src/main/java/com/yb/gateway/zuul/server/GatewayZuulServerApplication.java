package com.yb.gateway.zuul.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

//我们常用的就是@EnableZuulProxy,过多的过滤器也用不上
//@EnableZuulServer//开启路由(代理),高配版,拥有更多的过滤器,性能较@EnableZuulProxy低
@EnableZuulProxy//开启路由(代理),低配版,拥有更少的过滤器,性能较@EnableZuulServer高
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayZuulServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayZuulServerApplication.class, args);
    }

}
