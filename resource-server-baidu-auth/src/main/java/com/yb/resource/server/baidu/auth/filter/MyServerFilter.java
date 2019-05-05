package com.yb.resource.server.baidu.auth.filter;

import com.yb.common.server.dic.JwtDic;
import com.yb.common.server.other.LoginUser;
import com.yb.common.server.utils.JwtUtils;
import com.yb.common.server.utils.LoginUserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.stereotype.Component;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Description: SecurityContextPersistenceFilter --整个Spring Security 过滤器链的开端,它有两个作用:一是当请求到来时,
 * 检查 Session 中是否存在 SecurityContext ,如果不存在,就创建一个新的 SecurityContext ,二是请求结束时将 SecurityContext 放入 Session 中,
 * 并清空 SecurityContextHolder------------------------这个是通用版本
 * author biaoyang
 * date 2019/4/30 00309:32
 */
@Component
//public class UserServerFilter extends OncePerRequestFilter{//这个不是security的过滤器,所以去掉security的依赖没有依赖
public class MyServerFilter extends SecurityContextPersistenceFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        //获取请求头里的token信息
        String token = request.getHeader(JwtDic.HEADERS_NAME);
        //判断token是否存在,并校验其合法性,(checkAndGetPayload已经做了判空和前缀判断)
        LoginUser loginUser = JwtUtils.checkAndGetPayload(token, JwtDic.BASE64_ENCODE_SECRET);
        //判断是否能正确解析出放在荷载里的用户信息(验证签名不通过返回null)
        if (Objects.nonNull(loginUser)) {
            //获取登录用的ip地址
            String ipAddress = getIpAddress(request);
            //封装登录用户的ip地址
            loginUser.setIp(ipAddress);
            //实例化一个装权限/角色的集合
            Set<GrantedAuthority> roles = new HashSet<>(5);
            //判断用户是否带有权限/角色信息
            if (CollectionUtils.isNotEmpty(loginUser.getRoles())) {
                //注意这里需要为角色添加前缀,接口认证的时候是带有前缀的,否则匹配不上
                loginUser.getRoles().forEach(s -> roles.add(new SimpleGrantedAuthority(JwtDic.SECURITY_ROLE_PREFIX + s)));
            }
            //设置安全上下文信息
            Authentication authen = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), "", roles);
            //设置安全信息到上下文中
            SecurityContextHolder.getContext().setAuthentication(authen);
            //设置用户信息到LoginUserUtils里方便获取用户信息
            LoginUserUtils.setUser(loginUser);
        }
        //过滤请求
        chain.doFilter(req, res);
    }

    /**
     * 获取登录用户的ip地址
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String[] headers = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String ip = null;
        String unknown = "unknown";
        for (String header : headers) {
            ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !unknown.equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
