package com.yb.gateway.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author biaoyang
 * Date: 2019/4/25 0025
 * Description:
 */
@Profile({"dev", "test"})
@Component
@Primary//优先使用这个bean
public class SwaggerResourcesConfig implements SwaggerResourcesProvider {

    @Autowired
    private ApplicationPermitConfig permitConfig;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        permitConfig.getSwaggerResource()
                .forEach((name, location) -> {
                    resources.add(swaggerResource(name, location));
                });
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
