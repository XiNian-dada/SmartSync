package com.leeinx.smartsync.security;

import com.leeinx.smartsync.common.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析工具（基于 jjwt 0.12.x）。
 *
 * <h2>JWT 简介</h2>
 * 一个 JWT 字符串长这样：{@code xxxxx.yyyyy.zzzzz}
 * <ul>
 *   <li>第一段 Header：描述签名算法（HS256 等）。</li>
 *   <li>第二段 Payload：存放业务声明（claims），本项目放终端 ID 和编码。</li>
 *   <li>第三段 Signature：用密钥对前两段做 HMAC 签名，防止篡改。</li>
 * </ul>
 * <p>只要密钥不泄漏，拿到 token 的人改 Payload 后就无法重新生成正确的 Signature。</p>
 *
 * <h2>生命周期</h2>
 * 终端 POST /api/auth/login →  {@link #generate(Long, String)} 生成 token →
 * 每次请求带上 →  {@link JwtAuthenticationFilter} 调 {@link #parse(String)} 验证。
 *
 * <h2>{@link PostConstruct} 的意义</h2>
 * Spring 注入完 {@code @Value} 字段后，会回调所有 {@code @PostConstruct} 方法。
 * 我们在这里一次性完成"把 secret 字符串转成 {@link SecretKey} 对象"的初始化，后续请求直接用缓存好的 key 即可，性能更好。
 */
@Slf4j
@Component
public class JwtUtil {

    /** 从配置文件 {@code smartsync.jwt.secret} 注入的密钥原文。 */
    @Value("${smartsync.jwt.secret}")
    private String secret;

    /** JWT 有效期（小时），来自 {@code smartsync.jwt.expire-hours}。 */
    @Value("${smartsync.jwt.expire-hours}")
    private long expireHours;

    /** 缓存的签名 key，只在 {@link #init()} 里初始化一次。 */
    private SecretKey key;

    /**
     * Spring 注入完成后执行：把字符串密钥转成 HMAC-SHA256 需要的 {@link SecretKey}。
     * <p>HS256 算法要求密钥至少 256 bit（32 字节），这里在启动时就校验，发现问题"启动即失败"好过运行时报错。</p>
     *
     * @throws IllegalStateException 密钥长度不足时抛出，Spring 会让整个应用启动失败
     */
    @PostConstruct
    public void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("smartsync.jwt.secret 必须 >= 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 签发一个新 JWT。
     *
     * <h3>载荷字段说明</h3>
     * <ul>
     *   <li>{@code sub} (subject)：JWT 标准字段，放终端编码（人类可读的身份）。</li>
     *   <li>{@code tid}：自定义，放终端数据库主键（查 DB 用，比 code 快）。</li>
     *   <li>{@code tcode}：自定义，冗余一份终端编码，方便日志追踪。</li>
     *   <li>{@code iat} (issued at)：签发时间戳。</li>
     *   <li>{@code exp} (expiration)：过期时间戳，到期后 jjwt 会自动判定无效。</li>
     * </ul>
     *
     * @param terminalId   终端数据库主键
     * @param terminalCode 终端编码
     * @return 紧凑格式（compact）的 JWT 字符串
     */
    public String generate(Long terminalId, String terminalCode) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(terminalCode)
                .claim(SecurityConstants.CLAIM_TERMINAL_ID, terminalId)
                .claim(SecurityConstants.CLAIM_TERMINAL_CODE, terminalCode)
                .issuedAt(new Date(now))
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 JWT，一步完成签名验证 + 过期检查。
     * <p>返回 null 而不是抛异常，是为了把"token 无效"和"业务异常"区分开，调用方（Filter）可以直接判断 null 继续走无认证流程。</p>
     *
     * @param token 从 HTTP Header 提取的 token 字符串（已去除 "Bearer " 前缀）
     * @return 解析成功返回 {@link Claims}（可通过 {@code claims.get("tid", Long.class)} 等方法读取字段）；
     *         签名非法 / 过期 / 格式错误等情况返回 null
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
