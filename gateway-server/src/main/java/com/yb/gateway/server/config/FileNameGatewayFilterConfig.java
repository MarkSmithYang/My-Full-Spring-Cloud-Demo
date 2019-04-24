package com.yb.gateway.server.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;

/**
 * Description: 文件名(中文)设置
 * author biaoyang
 * date 2019/4/23 002318:09
 */
@Configuration
public class FileNameGatewayFilterConfig extends AbstractGatewayFilterFactory {

    private static final Logger log = LoggerFactory.getLogger(FileNameGatewayFilterConfig.class);

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();
            //获取文件名并判断
            String filename = request.getQueryParams().getFirst("attname");
            if (StringUtils.isBlank(filename)){
                return;
            }
            //处理文件名中文问题
            try {
                String codedFilename = java.net.URLEncoder.encode(filename, "UTF-8");
                String disposition = "attachment; " +
                        "fileName=\"" + codedFilename + "\"; " +
                        "filename*=utf-8''" + codedFilename;
                response.getHeaders()
                        .add("Content-Disposition", disposition);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }));
    }
}