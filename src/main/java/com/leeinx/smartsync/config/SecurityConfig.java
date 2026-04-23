package com.leeinx.smartsync.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security 入口配置。
 * 当前策略是无状态 JWT 鉴权，未登录和越权都返回统一 JSON。
 * 
 * 无状态指服务器将不会保存长期的用户信息, 每次请求都需要重新认证。扩展程度更好
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** 注册、登录和 Swagger 文档属于公开接口。 */
    private static final String[] PUBLIC_PATHS = { //这些接口都是公开接口, 不需要认证
            "/api/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error",
            "/favicon.ico"
    };

    /** 负责解析 JWT 并写入 SecurityContext。 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 用于在 Security 过滤器阶段手写 JSON 响应。 */
    private final ObjectMapper objectMapper;

    /** 密钥使用 BCrypt 存储，登录时只做匹配，不回存明文。 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 预留 AuthenticationManager，方便后续扩展其它认证方式。 */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * 定义 HTTP 安全策略。
     * 所有非公开接口都必须先通过 JWT 认证。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** 处理未认证请求，直接返回 JSON 401。 */
    private AuthenticationEntryPoint restAuthEntryPoint() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                Result.fail(ResultCode.UNAUTHORIZED));
    }

    /** 处理已认证但无权限的请求。 */
    private AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                Result.fail(ResultCode.FORBIDDEN));
    }

    /** Security 过滤器阶段不走 Controller Advice，因此要在这里手动输出 JSON。 */
    private void writeJson(HttpServletResponse resp, int status, Result<?> body) throws IOException {
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
