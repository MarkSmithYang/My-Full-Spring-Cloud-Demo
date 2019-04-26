package com.yb.gateway.server;

import com.alibaba.fastjson.JSONObject;
import com.yb.common.server.other.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import javax.validation.constraints.NotBlank;

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
        return "hello world";
    }

    @ApiOperation("不需要登录的测试接口")
    @GetMapping("/testNeedLogin")
    public String testNeedLogin() {
        return "hello world==testNeedLogin";
    }

    @ApiOperation("用来返回熔断信息的接口")
    @GetMapping("/fallback")
    public Mono<String> fallback() {
        Mono<String> mono = Mono.just("我熔断了哦");
        return mono;
    }
    //'&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

}
