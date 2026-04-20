package com.leeinx.smartsync.common.constant;

/**
 * 鉴权相关常量。
 *
 * <h2>为什么要单独放常量类</h2>
 * <ul>
 *   <li><b>避免魔法字符串</b>：代码里到处写 "Authorization" / "Bearer "，一旦想改（比如改为自定义 Header）要全局搜索替换。</li>
 *   <li><b>语义化</b>：{@code SecurityConstants.AUTH_HEADER} 比字面量 "Authorization" 更清楚表达"这是鉴权 Header"。</li>
 *   <li><b>一处定义，多处共享</b>：{@link com.leeinx.smartsync.security.JwtAuthenticationFilter} 和 {@link com.leeinx.smartsync.security.JwtUtil}
 *       都要引用这些值，放一起保证一致。</li>
 * </ul>
 *
 * <h2>final class + private 构造器</h2>
 * 这个惯用法（Effective Java item 4）确保这个类不可被继承、不可被实例化，纯工具用途。
 */
public final class SecurityConstants {

    /** 私有构造器，阻止外部 {@code new SecurityConstants()}（虽然 static 成员不需要实例，但兜底一下）。 */
    private SecurityConstants() {}

    /** HTTP Header 名 —— 终端请求必须在这里带 JWT，格式 {@code Authorization: Bearer xxx}。 */
    public static final String AUTH_HEADER = "Authorization";

    /** Bearer Token 的前缀（含末尾空格），业界标准写法（RFC 6750）。 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT 载荷里存"终端数据库 ID"的字段名，缩写 tid 让 Token 紧凑一些。 */
    public static final String CLAIM_TERMINAL_ID = "tid";

    /** JWT 载荷里存"终端编码"的字段名。 */
    public static final String CLAIM_TERMINAL_CODE = "tcode";

    /** Spring Security 的角色名惯例：以 {@code ROLE_} 开头。当前所有通过 JWT 鉴权的调用方统一使用此角色。 */
    public static final String ROLE_TERMINAL = "ROLE_TERMINAL";
}
