package com.yb.gateway.server.security;

import com.yb.gateway.server.filter.GatewayServerFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Description:
 * author biaoyang
 * date 2019/4/29 002912:50
 */
@EnableWebFluxSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class GatewaySecurityConfig {

    private final GatewayServerFilter gatewayServerFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        //需要认证的url请求时会弹出security的默认登录页,不知道如何关闭,这个可能是因为整合了webflux了吧
        http.cors().and().httpBasic().disable()
                //这里因为使用了过滤器,不需要通过这种方式进行验证,这里主要是让security放开所有url,让过滤器来处理
                //之所以需要配置这里,是因为必须引入security进行,登录状态的设置,为后面的接口方法上的接口权限认证提供条件
                //而security一旦引入就会拦截所有url,所以需要配置这个类,而这个网关默认使用的webflux,会与web依赖冲突,所以才是这样的配置
                .addFilterAt(gatewayServerFilter, SecurityWebFiltersOrder.FIRST)//优先使用过滤器
                //注意这里不能设置拦截,不然网关请求路径都会在这被拦截(即使过滤器放过了)
                .authorizeExchange().pathMatchers("/**").permitAll().anyExchange().authenticated();
        return http.build();
    }

}
