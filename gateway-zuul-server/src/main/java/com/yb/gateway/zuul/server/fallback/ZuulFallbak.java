package com.yb.gateway.zuul.server.fallback;

import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Description:zuul的熔断处理,FallbackProvider是springboot2.x的版本才有的,以前的那个好像是ZuulFallbackProvider,已经没了
 * --网关熔断配置
 * author biaoyang
 * date 2019/4/3 000320:07
 */
@Component
public class ZuulFallbak implements FallbackProvider {

    /**
     * 用于指定熔断功能应用于哪个路由服务
     * 至于还有没有其他的配置方式(肯定有),可以根据自己需要测试一下
     * @return
     */
    @Override
    public String getRoute() {
        //实现对producer-service服务的熔断
        //return "producer-service";
        //返回"*"星号实现对所有的路由服务的熔断
        return "*";
    }

    /**
     * 进入熔断功能时执行的逻辑
     *
     * @param route
     * @param cause
     * @return
     */
    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        return new ClientHttpResponse() {

            /**
             * 返回响应的HTTP状态代码
             * @return
             * @throws IOException
             */
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            /**
             * 将HTTP状态代码(可能是非标准的，并且不能通过HttpStatus enum解析)作为整数返回
             * @return
             * @throws IOException
             */
            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            /**
             * 返回响应的HTTP状态文本
             * @return
             * @throws IOException
             */
            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            /**
             * 关闭此响应，释放创建的任何资源
             */
            @Override
            public void close() {

            }

            /**
             * 将消息体作为输入流返回
             * @return
             * @throws IOException
             */
            @Override
            public InputStream getBody() throws IOException {
                //注意实测返回的信息必须是一个如下的json对象,实测把转义的双引号转成单引号,还是会解析失败
                return new ByteArrayInputStream(
                        "{\"status\":500,\"message\":\"请求服务被熔断了!\"}".getBytes());
            }

            /**
             * 返回此消息的头信息
             * 返回对应的HttpHeaders对象(从不为空)
             * @return
             */
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
