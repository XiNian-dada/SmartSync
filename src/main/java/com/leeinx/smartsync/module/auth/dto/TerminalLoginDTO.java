package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 终端登录请求体。
 *
 * <p>与 {@link TerminalRegisterDTO} 相比，登录时只要 code + secret 两个字段，没有 name、校验规则也更宽松（只判空）——
 * 因为注册时已经用过严格规则校验过，登录时再校验长度属于多余。</p>
 */
@Data
@Schema(description = "终端登录请求")
public class TerminalLoginDTO {

    /** 终端编码 */
    @Schema(description = "终端编码", example = "T001")
    @NotBlank
    private String terminalCode;

    /** 终端密钥（明文，服务器内部会和 DB 中的 BCrypt 哈希比对） */
    @Schema(description = "终端密钥")
    @NotBlank
    private String secretKey;
}
