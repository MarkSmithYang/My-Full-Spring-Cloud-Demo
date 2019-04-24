package com.yb.gateway.server.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Description: 网关服务配置
 * author biaoyang
 * date 2019/4/23 00239:59
 */
@Configuration
public class GatewayServerConfig {

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(s ->
                        s.path("/world1")
                                .filters(f -> {
                                    //实测请求头里设置在浏览器上看不到
                                    f.addRequestHeader("aa", UUID.randomUUID().toString().replaceAll("-", ""));
                                    f.setRequestHeader("lalal", "pppppppppp");
                                    //实测重定向的时间直接是重定向到百度的网页,调换位置后,直接到/test,也就是它以最后的重定向为准
                                    f.redirect(HttpStatus.FOUND.value(), "http://192.168.2.233:9094/test");
                                    //而且这个redirect的int status参数必须是3开头的状态码3xx,一般302即可
                                    f.redirect(HttpStatus.FOUND.value(), "https://www.baidu.com");
                                    //因为T是继承R的,所以返回T也是可以的
                                    return f;
                                })
                                //只要上面设置了重定向,那么这个uri就是不会走的
                                .uri("http://localhost:9002"))
                .route(s ->
                        //注意这里的请求是/hello只要是下面那个服务有的就行,至于不带参数的话,会和在下面的那个服务一样报缺少参数异常
                        s.path("/hello")
                                .filters(f -> {
                                    //当请求的服务发生异常时,直接熔断返回下面设置url(可以是视图view),这个就相当于对单个请求做了熔断
                                    f.hystrix(g -> g.setFallbackUri("forward:/fallback"));
                                    //实测这个添加的参数如果是中文就会报错,是英文就没事,还有就是,如果在请求地址后面再拼接参数,
                                    //那个拼接的参数会和这个参数一起,用逗号分隔,所以建议不要在这里设置参数,这里也写死了,不想url灵活
//                                    f.addRequestParameter("name", "xxx");
                                    return f;
                                    //需要含有上面对应接口的地址
                                }).uri("http://localhost:9002"))
                //实测跳不到百度的正常页面去,因为https://www.baidu.com/hello根本请求不了什么东西
                //}).uri("https://www.baidu.com"))
                .build();
    }

}
