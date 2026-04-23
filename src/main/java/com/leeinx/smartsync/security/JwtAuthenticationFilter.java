package com.leeinx.smartsync.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.leeinx.smartsync.common.constant.SecurityConstants;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 每个请求只执行一次的 JWT 认证过滤器。
 * 成功解析后，会把终端身份写入 SecurityContext。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 解析工具。 */
    private final JwtUtil jwtUtil; // 自动被注入

    /** 从 Header 中提取 Bearer Token，解析成功后建立终端认证信息。 */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        //从请求体中取出 header部分                                
        String header = request.getHeader(SecurityConstants.AUTH_HEADER);
        //如果header中存在Bearer Token
        if (StringUtils.hasText(header) && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = header.substring(SecurityConstants.TOKEN_PREFIX.length());
            //丢给jwt校验
            Claims claims = jwtUtil.parse(token);
            if (claims != null) { // 校验通过
                Long tid = claims.get(SecurityConstants.CLAIM_TERMINAL_ID, Long.class); //获取到终端 ID
                String tcode = claims.get(SecurityConstants.CLAIM_TERMINAL_CODE, String.class); // 获取终端编码
                if (tid != null && StringUtils.hasText(tcode) // 处理意外情况 终端有效再继续
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    TerminalPrincipal principal = new TerminalPrincipal(tid, tcode);
                    // 认证已经由 JWT 完成，这里不再保存明文凭证。
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority(SecurityConstants.ROLE_TERMINAL)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth); //把认证数据放到 SecurityContext 中
                }
            }
        }
        chain.doFilter(request, response);
    }
}
