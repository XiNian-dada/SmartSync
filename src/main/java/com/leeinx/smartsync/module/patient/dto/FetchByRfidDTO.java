package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 终端按 RFID 拉取患者时的请求体。
 * 详细的格式和校验位验证交给 RfidUtil 处理。
 */
@Data
@Schema(description = "终端按 RFID 拉取患者")
public class FetchByRfidDTO {

    /** 终端读到的 13 位 RFID 原始值。 */
    @Schema(description = "13 位 RFID 字符串（12 位 UUID + 1 位校验位）", example = "ABCDEFGHJKMNX")
    @NotBlank
    private String rfid;
}
