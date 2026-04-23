package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 终端登录请求体。 */
@Data
@Schema(description = "终端登录请求")
public class TerminalLoginDTO {

    /** 登录账号。 */
    @Schema(description = "终端编码", example = "T001")
    @NotBlank
    private String terminalCode;

    /** 明文密钥，会与数据库中的 BCrypt 哈希做匹配。 */
    @Schema(description = "终端密钥")
    @NotBlank
    private String secretKey;
}
