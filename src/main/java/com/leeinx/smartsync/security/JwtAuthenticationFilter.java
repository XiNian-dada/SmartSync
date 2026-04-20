package com.leeinx.smartsync.security;

import com.leeinx.smartsync.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：在 Filter Chain 中提取 JWT 并填充 Spring Security 的"认证上下文"。
 *
 * <h2>在整个鉴权链路中的位置</h2>
 * <pre>
 *   HTTP 请求
 *     ↓
 *   Spring Security FilterChain
 *     ├── CorsFilter
 *     ├── CsrfFilter（已禁用）
 *     ├── [本 Filter] JwtAuthenticationFilter  ← 解析 Bearer token，填充 SecurityContext
 *     ├── UsernamePasswordAuthenticationFilter（本项目不用，但仍在链上）
 *     ├── ExceptionTranslationFilter
 *     ├── AuthorizationFilter                   ← 检查当前用户有没有访问该路径的权限
 *     ↓
 *   Dispatcher Servlet → Controller
 * </pre>
 *
 * <h2>{@link OncePerRequestFilter}</h2>
 * Servlet 规范允许同一个 Filter 在请求转发时被触发多次。继承 {@code OncePerRequestFilter} 可以保证单次请求只执行一次
 * {@code doFilterInternal}，避免重复解析 JWT。
 *
 * <h2>{@code @Component}</h2>
 * 让 Spring 把这个 Filter 当作 Bean 管理，然后在 {@link com.leeinx.smartsync.config.SecurityConfig} 里通过构造器注入使用。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 工具，通过构造器注入（{@code @RequiredArgsConstructor} 生成的构造器）。 */
    private final JwtUtil jwtUtil;

    /**
     * Filter 核心逻辑，每个请求都会进来一次。
     *
     * <h3>执行步骤</h3>
     * <ol>
     *   <li>从 Header 取 {@code Authorization}。</li>
     *   <li>判断是否 {@code Bearer xxx} 格式，不是就跳过（放行给后续 Filter，让 SecurityConfig 的白名单 / 401 处理器决定）。</li>
     *   <li>截掉前缀，用 {@link JwtUtil#parse(String)} 解析。</li>
     *   <li>解析成功则从 claims 里拿 tid / tcode，构造 {@link TerminalPrincipal}。</li>
     *   <li>构造 {@link UsernamePasswordAuthenticationToken} 放入 {@link SecurityContextHolder}——
     *       这一步相当于告诉 Security："当前请求已登录，主体是这个终端"。</li>
     *   <li>调 {@code chain.doFilter} 让请求继续流转（即使 token 无效也要调，让后面的 Filter/Handler 统一处理）。</li>
     * </ol>
     *
     * <h3>为什么要检查 {@code getAuthentication() == null}</h3>
     * 防止前面的 Filter 已经填充过认证（极少见，但保持 idempotent 是好习惯）。
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    Filter 链
     * @throws ServletException Servlet 容器异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = header.substring(SecurityConstants.TOKEN_PREFIX.length());
            Claims claims = jwtUtil.parse(token);
            if (claims != null) {
                Long tid = claims.get(SecurityConstants.CLAIM_TERMINAL_ID, Long.class);
                String tcode = claims.get(SecurityConstants.CLAIM_TERMINAL_CODE, String.class);
                if (tid != null && StringUtils.hasText(tcode)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    TerminalPrincipal principal = new TerminalPrincipal(tid, tcode);
                    // 第二个参数 credentials（凭证）设为 null：认证已完成，token 本身不再需要保存
                    // 第三个参数是授予的权限列表，这里统一给 ROLE_TERMINAL
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority(SecurityConstants.ROLE_TERMINAL)));
                    // 附加请求 IP、Session ID 等信息（Spring Security 默认做法）
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
