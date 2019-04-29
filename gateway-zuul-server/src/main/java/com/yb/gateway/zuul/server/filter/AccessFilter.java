package com.yb.gateway.zuul.server.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.LoginUserUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * -------------------网关这里做的过滤并不能很好的处理访问的开放的问题,例如登录页面的放开,这里就没法弄,
 * -------------------网关必须要使用web
 * 即便是配置了安全配置,那边放过的url还是需要过滤器处理,才能通过,否则还是会被拦住,所以配置了也没有意义
 * Description:自定义过滤器,仅仅只需继承抽象类ZuulFilter,实现/重写其方法即可
 * author biaoyang
 * date 2019/4/3 000319:52
 */
@RefreshScope//这个是个坑,需要注解在有获取配置的类上,才能让获取的配置读取的是最新的配置,
// 否则即便console里提示已经刷新,你实际还是获取到的旧的配置(实测)
@Component//把过滤器实例化到spring容器里,也可以通过@Bean注解的方法实例化
public class AccessFilter extends ZuulFilter {
    //这个配置已经配置在云配置上
    @Value("${gateway.permit}")
    private String[] permitUrl;

    private final PathMatcher pathMatcher;

    public AccessFilter() {
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * Zuul有一下四种过滤器
     * "pre":是在请求路由到具体的服务之前执行,这种类型的过滤器可以做安全校验,例如身份校验,参数校验等
     * "routing":它用于将请求路由到具体的微服务实例,在默认情况下,它使用Http Client进行网络请求
     * "post":它是在请求已被路由到微服务后执行,一般情况下,用作收集统计信息,指标,以及将响应传输到客户端
     * "error":它是在其他过滤器发生错误时执行
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤顺序,值越小,越早执行该过滤器
     * 指定该Filter执行的顺序（Filter从小到大执行）
     * DEBUG_FILTER_ORDER = 1;
     * FORM_BODY_WRAPPER_FILTER_ORDER = -1;
     * PRE_DECORATION_FILTER_ORDER = 5;
     * RIBBON_ROUTING_FILTER_ORDER = 10;
     * SEND_ERROR_FILTER_ORDER = 0;
     * SEND_FORWARD_FILTER_ORDER = 500;
     * SEND_RESPONSE_FILTER_ORDER = 1000;
     * SIMPLE_HOST_ROUTING_FILTER_ORDER = 100;
     * SERVLET_30_WRAPPER_FILTER_ORDER = -2;
     * SERVLET_DETECTION_FILTER_ORDER = -3;
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 表示该过滤器是否过滤逻辑,如果是ture,则执行run()方法;如果是false,则不执行run()方法
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //可以在这里添加逻辑代码,一般来说都没有必要
        return true;
    }

    /**
     * 这里做安全校验,身份校验,一般做token合法性校验,获取登录用户信息,存入redis
     * (存储jwt的jti到redis,可以让其随时失效)或者存入InheritableThreadLocal
     * InheritableThreadLocal当前线程创建子线程时,子线程能够继承父线程中的ThreadLocal变量;
     *
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //获取请求对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        //获取请求的url的地址,例如http://localhost:9095/producer/hello?name=小明,获取到的是/producer/hello
        String path = request.getRequestURI();//肯定不为空
        //判断该uri是否是不需要登录的
        if (ArrayUtils.isNotEmpty(permitUrl)) {
            for (String pattern : permitUrl) {
                if (pathMatcher.match(pattern, path)) {
                    //如果匹配上则,结束方法
                    return null;
                }
            }
        }
        //获取请求头里的token信息-->这里默认只认key为Authorization的头,jwt都是用这个
        String token = request.getHeader("Authorization");
        //判断token的合法性
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            LoginUser loginUser = new LoginUser();
            loginUser.setUsername("rose");
            Set<String> set = new HashSet<>(5);
            set.add("admin");
            set.add("boss");
            loginUser.setRoles(set);
            LoginUserUtils.setUser(loginUser);
            //通过Jwt工具验证签名
            //通过则解析荷载,把用户信息存入到对应的地方
            //生成jti绑定jwt并存入redis
        } else {
            //这里根之前使用的那个AuthorizationEntryPoint的实现类返回的信息差不多的
            //不会继续往下执行,不会调用服务接口了,网关直接响应给客户了
            ctx.setSendZuulResponse(false);
            //设置响应码
            ctx.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);//401
            //让浏览器用utf8来解析返回的数据,需要和下面的setCharacterEncoding的编码保持一致
            ctx.addZuulResponseHeader("Content-type", "text/html;charset=UTF-8");
            //告诉servlet用UTF-8转码，而不是用默认的ISO8859-1,这个需要在写中文前设置
            ctx.getResponse().setCharacterEncoding("UTF-8");
            //可以直接返回提示请登录,也可以字符串化一个对象返回
            ctx.setResponseBody("请登录");
            //还可以使用下面这种方式,只是下面的需要处理异常,不推荐
            //ctx.getResponse().getWriter().write("请登录");
        }
        return null;
    }
}
