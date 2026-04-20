package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 终端按 RFID 拉取患者数据的请求体。
 *
 * <p>只有一个字段 {@code rfid}，但仍然用 DTO 包一层——
 * 比直接 {@code @RequestParam String rfid} 好在：请求体未来要加字段（如 nonce、signature 防重放）时不改接口签名。</p>
 *
 * <h2>为什么不在这里做 13 位正则校验</h2>
 * 13 位校验涉及到 HMAC 计算，单纯 {@code @Pattern} 只能判长度和字符集，真正的完整性校验交给
 * {@link com.leeinx.smartsync.util.RfidUtil#verify(String)}，业务语义更清晰。
 */
@Data
@Schema(description = "终端按 RFID 拉取患者")
public class FetchByRfidDTO {

    /** 13 位 RFID 字符串，由终端从手环读出后原样上传 */
    @Schema(description = "13 位 RFID 字符串（12 位 UUID + 1 位校验位）", example = "ABCDEFGHJKMNX")
    @NotBlank
    private String rfid;
}
