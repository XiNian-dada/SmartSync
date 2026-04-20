package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录成功返回值（View Object）。
 *
 * <h2>DTO vs VO</h2>
 * 项目内一般用两种叫法区分入参/出参：
 * <ul>
 *   <li><b>DTO</b>（Data Transfer Object）——通常指<b>入参</b>，客户端 → 服务端。</li>
 *   <li><b>VO</b>（View Object）——通常指<b>出参</b>，服务端 → 客户端。</li>
 * </ul>
 * 两者本质都是简单的数据载体，分开命名只是为了阅读代码时能一眼看出方向。
 */
@Data
@Schema(description = "登录返回 Token")
public class LoginVO {

    /** JWT Token 字符串，客户端要保存下来，之后请求带在 Header 里 */
    @Schema(description = "Bearer Token")
    private String token;

    /** Token 过期秒数，方便客户端估算是否要刷新 */
    @Schema(description = "过期秒数")
    private Long expiresIn;

    /** 回显终端编码，客户端可以校验是否与自己记录的一致 */
    @Schema(description = "终端编码")
    private String terminalCode;
}
