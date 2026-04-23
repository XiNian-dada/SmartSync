package com.leeinx.smartsync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * OpenAPI 文档配置。
 * 主要补充接口元信息，以及 Swagger UI 里的 Bearer Token 鉴权方案。
 * 
 * 用于文档生成。
 */
@Configuration
public class OpenApiConfig {

    /** OpenAPI 中引用的 Bearer 鉴权方案名。 */
    private static final String BEARER_SCHEME = "bearer-jwt"; 

    /** 注册项目的 OpenAPI 元信息和全局鉴权配置。 */
    @Bean
    public OpenAPI smartSyncOpenAPI() {
        return new OpenAPI()

                //定义基本接口信息
                .info(new Info()
                        .title("SmartSync API") //接口的相关信息
                        .version("0.0.1")
                        .description("医院分布式终端 / RFID 数据下发服务"))
                // 默认要求接口携带 JWT，登录注册接口可在控制器侧单独放开。
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)));
    }
}
