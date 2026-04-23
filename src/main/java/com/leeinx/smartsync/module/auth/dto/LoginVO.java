package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 登录成功后返回给终端的数据。 */
@Data
@Schema(description = "登录返回 Token")
public class LoginVO {

    /** JWT 字符串。 */
    @Schema(description = "Bearer Token")
    private String token;

    /** 剩余有效期，单位秒。 */
    @Schema(description = "过期秒数")
    private Long expiresIn;

    /** 当前登录终端编码。 */
    @Schema(description = "终端编码")
    private String terminalCode;
}
