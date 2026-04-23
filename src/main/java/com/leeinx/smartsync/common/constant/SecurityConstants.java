package com.leeinx.smartsync.common.constant;

/** 鉴权链路里复用的固定字符串。 */
public final class SecurityConstants {
    //声明一些前缀, 用于从Header中剥离数据

    /** 常量类不需要实例。 */
    private SecurityConstants() {}

    /** JWT 所在的请求头。 */
    public static final String AUTH_HEADER = "Authorization";

    /** Bearer Token 前缀。 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT 中保存终端主键的 claim 名。 */
    public static final String CLAIM_TERMINAL_ID = "tid";

    /** JWT 中保存终端编码的 claim 名。 */
    public static final String CLAIM_TERMINAL_CODE = "tcode";

    /** 当前系统统一赋给终端的角色。 */
    public static final String ROLE_TERMINAL = "ROLE_TERMINAL";
}
