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
 * JWT 的签发与解析工具。
 * token 中保存终端主键和终端编码，供后续请求恢复登录身份。
 */
@Slf4j
@Component
//组件声明 受到Spring管理生命周期
public class JwtUtil {

    /** 原始密钥字符串。 */
    @Value("${smartsync.jwt.secret}")
    private String secret;

    /** token 过期时长，单位小时。 */
    @Value("${smartsync.jwt.expire-hours}")
    private long expireHours;

    /** 启动时预先构造好的签名 key。 */
    private SecretKey key;

    /** 启动时校验 JWT 密钥长度，并转换成 jwt 所需的 SecretKey。 */
    @PostConstruct
    public void init() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("smartsync.jwt.secret 必须 >= 32 字节");
        }
        //因为jwt库的方法签名要求的传入参数必须是Secret Key s所以通过 SHA256算法生成一个Secret Key
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    /** 签发一个新 JWT，供终端后续请求使用。 */
    public String generate(Long terminalId, String terminalCode) {
        long now = System.currentTimeMillis();
        Date exp = new Date(now + expireHours * 3600_000L);//过期时间
        return Jwts.builder() // 返回签发的 JWT
                .subject(terminalCode) // 给谁签发
                .claim(SecurityConstants.CLAIM_TERMINAL_ID, terminalId) // 自定义字段
                .claim(SecurityConstants.CLAIM_TERMINAL_CODE, terminalCode)
                .issuedAt(new Date(now)) // 签发的时间
                .expiration(exp) //过期时间
                .signWith(key)// 签名key 即上面通过bytes计算的Secret Key
                .compact(); //生成并返回
    }

    /**
     * 解析并校验 JWT。
     * 返回 null 表示 token 无效，由过滤器继续按未登录请求处理。
     */
    public Claims parse(String token) { // 解析并返回Claims部分
        try {
            return Jwts.parser()//初始化解析器
                    .verifyWith(key).build() //使用key
                    .parseSignedClaims(token) //验证 token
                    .getPayload(); //返回Claims
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
