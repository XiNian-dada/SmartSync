package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者新建/更新入参。
 * 同一套 DTO 同时服务于新增和编辑页面。
 */
@Data
@Schema(description = "患者新建/更新")
public class PatientSaveDTO {

    /** 当前绑定手环，可为空。 */
    @Schema(description = "当前手环 12 位 UUID（Base32 大写字符）；不绑定则传 null")
    @Pattern(regexp = "^[0-9A-V]{12}$", message = "rfidUuid 必须是 12 位 Base32 大写字符")
    private String rfidUuid;

    /** 身份证号，当前只校验 18 位格式。 */
    @Schema(description = "身份证号（18 位）", example = "110101199003078888")
    @NotBlank
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式非法")
    private String idCardNo;

    /** 医保卡号，可选。 */
    @Schema(description = "医保卡号")
    @Size(max = 64)
    private String insuranceNo;

    /** 本人手机号，按大陆 11 位号码校验。 */
    @Schema(description = "本人手机号（中国大陆 11 位）", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式非法")
    private String phone;

    /** 患者姓名。 */
    @Schema(description = "姓名")
    @NotBlank
    @Size(max = 64)
    private String name;

    /** 性别：1 男，2 女。 */
    @Schema(description = "1男 2女")
    @Min(1)
    @Max(2)
    private Integer gender;

    /** 年龄。 */
    @Min(0)
    @Max(200)
    private Integer age;

    /** 长期病史信息。 */
    @Schema(description = "病史：慢病、过敏、既往手术等长期健康背景")
    private String medicalHistory;

    /** 紧急联系人姓名。 */
    @Schema(description = "紧急联系人姓名")
    @Size(max = 64)
    private String emergencyContactName;

    /** 紧急联系人电话。 */
    @Schema(description = "紧急联系人电话")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "紧急联系人电话格式非法")
    private String emergencyContactPhone;
}
