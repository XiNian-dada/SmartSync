package com.leeinx.smartsync.module.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 终端自助注册请求体。
 *
 * <h2>DTO 分层的意义</h2>
 * 不直接用 {@link com.leeinx.smartsync.module.terminal.entity.Terminal} 当入参：
 * <ul>
 *   <li>Entity 里有 {@code secretHash}（已加密）、{@code status}、{@code lastLoginAt} 等字段——调用方不应该传，
 *       否则恶意客户端可能直接设置 {@code status=1} 跳过审核。</li>
 *   <li>DTO 只暴露"客户端该填什么"，保护 Entity 字段。</li>
 *   <li>校验注解（{@code @NotBlank} 等）放在 DTO 上，不污染 Entity。</li>
 * </ul>
 *
 * <h2>注解用途</h2>
 * <ul>
 *   <li>{@code @Schema}（SpringDoc）——生成 Swagger 页面时作为字段描述，对代码逻辑无影响。</li>
 *   <li>{@code @NotBlank} / {@code @Size}（Jakarta Bean Validation）——Controller 层 {@code @Valid} 时触发校验，
 *       不通过则抛 {@link org.springframework.web.bind.MethodArgumentNotValidException}。</li>
 * </ul>
 */
@Data
@Schema(description = "终端自助注册请求")
public class TerminalRegisterDTO {

    /** 终端编码，作为登录账号，全局唯一 */
    @Schema(description = "终端编码（唯一）", example = "T001")
    @NotBlank
    @Size(min = 2, max = 64)
    private String terminalCode;

    /** 终端名称，方便管理员识别，可选 */
    @Schema(description = "终端显示名称", example = "住院部3楼-RFID读卡器")
    @Size(max = 128)
    private String terminalName;

    /**
     * 终端密钥（相当于密码）。
     * <p>服务器收到后会 BCrypt 加密后存 DB，不明文保存。客户端自己保存明文用于后续登录。</p>
     */
    @Schema(description = "终端密钥（终端本地保存）")
    @NotBlank
    @Size(min = 8, max = 128)
    private String secretKey;
}
