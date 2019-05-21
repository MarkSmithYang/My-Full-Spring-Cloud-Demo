package com.yb.mybatis.plus.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author biaoyang
 */
@SpringBootApplication
//扫描mybatis的接口包,mybatis必不可少
@MapperScan("com.yb.mybatis.plus.server.mapper")
public class MybatisPlusServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusServerApplication.class, args);
    }

}

