package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 终端注册请求体。 */
@Data
@Schema(description = "终端自助注册请求")
public class TerminalRegisterDTO {

    /** 终端唯一编码，也是登录账号。 */
    @Schema(description = "终端编码（唯一）", example = "T001")
    @NotBlank
    @Size(min = 2, max = 64)
    private String terminalCode;

    /** 终端显示名称，可选。 */
    @Schema(description = "终端显示名称", example = "住院部3楼-RFID读卡器")
    @Size(max = 128)
    private String terminalName;

    /** 终端本地保存的登录密钥，服务端仅存哈希。 */
    @Schema(description = "终端密钥（终端本地保存）")
    @NotBlank
    @Size(min = 8, max = 128)
    private String secretKey;
}
