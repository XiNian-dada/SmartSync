package com.leeinx.smartsync.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 文档配置。
 *
 * <h2>SpringDoc 做了什么</h2>
 * 引入 {@code springdoc-openapi-starter-webmvc-ui} 后，框架会自动扫描所有 {@code @RestController}，根据方法签名、
 * {@code @Operation} / {@code @Schema} 等注解生成 OpenAPI 3 规范的 JSON，并在 {@code /swagger-ui.html} 渲染成可视化页面。
 *
 * <h2>这里定义的 OpenAPI Bean 补充了两件事</h2>
 * <ol>
 *   <li><b>API 元信息</b>：标题、版本、描述——让 Swagger 页面顶部显示项目信息。</li>
 *   <li><b>全局 Bearer 鉴权方案</b>：声明所有接口都需要在 Header 里带 JWT。Swagger UI 上会出现 "Authorize" 按钮，
 *       填入 token 后，后续请求自动加 {@code Authorization: Bearer xxx} Header。</li>
 * </ol>
 */
@Configuration
public class OpenApiConfig {

    /** 鉴权方案的名字，同一个 OpenAPI 文档内唯一；在 {@code addSecurityItem} 和 {@code addSecuritySchemes} 之间要保持一致。 */
    private static final String BEARER_SCHEME = "bearer-jwt";

    /**
     * 构造自定义的 OpenAPI 描述对象。
     * <p>SpringDoc 会以这个 Bean 作为文档基础，再合并从 Controller 扫描出的接口定义。</p>
     *
     * @return 包含元信息 + 全局 Bearer 鉴权的 OpenAPI 配置
     */
    @Bean
    public OpenAPI smartSyncOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartSync API")
                        .version("0.0.1")
                        .description("医院分布式终端 / RFID 数据下发服务"))
                // 声明"全局鉴权要求"：除非接口单独覆盖，所有接口都要满足这个要求
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                // 定义鉴权的具体方案：HTTP Bearer + JWT 格式
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)));
    }
}
