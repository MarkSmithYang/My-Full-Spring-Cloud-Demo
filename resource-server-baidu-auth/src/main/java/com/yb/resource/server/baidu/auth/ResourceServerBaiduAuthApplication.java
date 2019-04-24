package com.yb.resource.server.baidu.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ResourceServerBaiduAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerBaiduAuthApplication.class, args);
    }

}
