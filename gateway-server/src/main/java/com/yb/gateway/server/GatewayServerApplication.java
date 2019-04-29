package com.yb.gateway.server;

import com.yb.common.server.utils.LoginUserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Set;

@Api(tags = "网关的启动类,也是接口类")
@Validated
@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }

    //'&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    //仅供GatewayServerConfig测试使用的接口
    @ApiOperation("不需要登录的测试接口")
    @GetMapping("/test")
    public String test() {
        return "hello world==";
    }

    //@PreAuthorize("hasRole('admin')"),这个看源码它会自动加前缀的,
    //所以构造权限进去的时候也需要添加上相同的前缀,所以最好都去看下源码是啥前缀
    @PreAuthorize("hasRole('admin')")
    @ApiOperation("不需要登录的测试接口")
    @GetMapping("/testNeedLogin")
    public String testNeedLogin(Principal principal) {
        Set<String> strings = LoginUserUtils.getRoles().orElse(null);
        System.err.println(strings==null?0:strings);
        return "hello world==testNeedLogin=="+principal;
    }

    @ApiOperation("用来返回熔断信息的接口")
    @GetMapping("/fallback")
    public Mono<String> fallback() {
        Mono<String> mono = Mono.just("我熔断了哦");
        return mono;
    }
    //'&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

}
