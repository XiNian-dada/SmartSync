package com.leeinx.smartsync.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

import java.io.IOException;

/**
 * Spring Security 核心配置。
 *
 * <h2>Security 是"Filter Chain"模型</h2>
 * <p>每个请求都会依次经过一串 Filter。Spring Security 本质是往 Servlet 的 Filter Chain 里插入多个
 * 安全相关的 Filter（CSRF、CORS、认证、授权...），本文件就是定义这条链怎么拼。</p>
 *
 * <h2>关键设计决策</h2>
 * <ol>
 *   <li><b>无状态（STATELESS）</b>：不用服务器 Session，完全靠 JWT 携带身份，适合分布式部署和终端设备。</li>
 *   <li><b>JWT Filter 插在 UsernamePasswordAuthenticationFilter 之前</b>：
 *       Spring Security 默认会用 {@link UsernamePasswordAuthenticationFilter} 处理表单登录，我们不需要，但用它作为"插桩锚点"。</li>
 *   <li><b>认证失败/越权的响应</b>：统一返回 JSON（而不是 302 跳登录页），因为调用方是 API 消费者。</li>
 * </ol>
 *
 * <h2>{@code @RequiredArgsConstructor}（Lombok）</h2>
 * 自动为所有 {@code final} 字段生成构造器。Spring 会通过构造器注入这些依赖（{@code jwtAuthenticationFilter / objectMapper}）。
 * 这是 Spring 推荐的注入方式（比 {@code @Autowired} 字段注入更利于测试 / 避免 NPE）。
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 不需要登录就能访问的路径白名单。
     * <ul>
     *   <li>{@code /api/auth/**} —— 注册/登录本身不能要求已登录</li>
     *   <li>{@code /swagger-ui/**} {@code /v3/api-docs/**} —— API 文档页面</li>
     *   <li>{@code /error} —— Spring Boot 默认错误页，放开避免 401→500 循环</li>
     * </ul>
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error",
            "/favicon.ico"
    };

    /** JWT 认证过滤器，在 Filter Chain 中解析 token 并填充认证上下文 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Jackson 的 JSON 序列化工具，用于把错误响应写回 HTTP Body */
    private final ObjectMapper objectMapper;

    /**
     * 注册密码编码器 Bean。
     *
     * <h3>为什么用 BCrypt</h3>
     * <ul>
     *   <li>单向不可逆（无法解密，只能比对）</li>
     *   <li>自带 salt（不需要额外存 salt 字段）</li>
     *   <li>可调工作因子（慢哈希，抗暴力破解）</li>
     * </ul>
     *
     * <h3>Spring Security 约定</h3>
     * 容器里只要有一个 {@link PasswordEncoder} Bean，Security 就会用它来校验密码。
     *
     * @return BCrypt 实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 暴露 {@link AuthenticationManager} 为 Bean。
     * <p>本项目当前没用 Security 内置的 login 流程（我们自己在 {@code AuthServiceImpl} 里做密码校验），
     * 但预留此 Bean 方便将来扩展（例如接入 LDAP / OAuth2）。</p>
     *
     * @param cfg Spring Security 自动装配的 {@link AuthenticationConfiguration}
     * @return 容器管理的 AuthenticationManager
     * @throws Exception 配置异常（几乎不会发生，照 Spring 规范声明）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /**
     * 定义 HTTP 安全策略（本项目的核心配置方法）。
     *
     * <h3>逐行说明</h3>
     * <ul>
     *   <li>{@code csrf.disable()} —— 关闭 CSRF 保护。CSRF 是针对浏览器 Cookie 的攻击手段，JWT 不存 Cookie，不受影响。</li>
     *   <li>{@code cors.withDefaults()} —— 启用默认 CORS 策略。如果前端分离部署在其他域名，需要额外写 {@code CorsConfigurationSource} Bean。</li>
     *   <li>{@code sessionManagement.STATELESS} —— 告诉 Security 不要创建 HttpSession，每次请求都独立鉴权。</li>
     *   <li>{@code authorizeHttpRequests} —— 定义 URL 权限规则：
     *     <ul>
     *       <li>OPTIONS 请求全放行（浏览器 CORS 预检）</li>
     *       <li>{@link #PUBLIC_PATHS} 放行</li>
     *       <li>其余所有路径都必须通过认证</li>
     *     </ul>
     *   </li>
     *   <li>{@code exceptionHandling} —— 自定义认证失败/越权的响应格式（返回标准 {@link Result} JSON）。</li>
     *   <li>{@code addFilterBefore} —— 把我们的 JWT Filter 插到默认的表单登录 Filter 之前，让它先执行。</li>
     * </ul>
     *
     * @param http Spring Security 提供的 HTTP 配置构建器
     * @return 构建好的 Filter Chain，Spring 会自动注册到 Servlet 容器
     * @throws Exception 构建异常
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

    /**
     * 认证入口点：处理<b>未认证</b>的请求（没 token / token 无效 / token 过期）。
     * <p>返回 401 + 标准 JSON，覆盖 Spring 默认的"重定向到登录页"行为。</p>
     *
     * @return lambda 形式的 AuthenticationEntryPoint
     */
    private AuthenticationEntryPoint restAuthEntryPoint() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                Result.fail(ResultCode.UNAUTHORIZED));
    }

    /**
     * 访问拒绝处理器：处理<b>已认证但无权限</b>的请求。
     * <p>当前所有终端都是 {@code ROLE_TERMINAL}，暂时用不到；等引入多角色后会生效（如普通终端访问管理员接口）。</p>
     */
    private AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                Result.fail(ResultCode.FORBIDDEN));
    }

    /**
     * 把 {@link Result} 对象以 JSON 形式写回响应体。
     * <p>为什么不直接用 {@code @ExceptionHandler}：Security Filter 在 Spring MVC Dispatcher 之前执行，
     * 异常不会被 Controller 的 {@code @RestControllerAdvice} 捕获，必须在这里手动写响应。</p>
     *
     * @param resp   Servlet 响应对象
     * @param status HTTP 状态码（401 / 403）
     * @param body   要序列化的业务响应
     * @throws IOException 写入失败
     */
    private void writeJson(HttpServletResponse resp, int status, Result<?> body) throws IOException {
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
