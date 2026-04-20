package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 患者新建 / 更新共用入参。
 *
 * <h2>字段分组</h2>
 * <ol>
 *   <li>RFID 手环：{@code rfidUuid}（可选——注册档案时可能还没分配手环）</li>
 *   <li>实名信息：{@code idCardNo / insuranceNo / phone}</li>
 *   <li>基本信息：{@code name / gender / age}</li>
 *   <li>病史：{@code medicalHistory}</li>
 *   <li>紧急联系人：{@code emergencyContactName / emergencyContactPhone}</li>
 * </ol>
 *
 * <h2>身份证号正则</h2>
 * 18 位标准公民身份号：17 位数字 + 校验位（数字或 X）。留意：
 * <ul>
 *   <li>当前只做格式校验，未做真实性验证（GB 11643 校验位算法）。</li>
 *   <li>未兼容 15 位老身份证号（已基本淘汰）。</li>
 * </ul>
 *
 * <h2>手机号正则</h2>
 * 中国大陆号段：1 开头 + 第二位 3~9 + 后 9 位数字。不处理国际号码，生产场景建议用 libphonenumber。
 */
@Data
@Schema(description = "患者新建/更新")
public class PatientSaveDTO {

    /** 当前手环 UUID，可选；为空表示暂不绑定 */
    @Schema(description = "当前手环 12 位 UUID（Base32 大写字符）；不绑定则传 null")
    @Pattern(regexp = "^[0-9A-V]{12}$", message = "rfidUuid 必须是 12 位 Base32 大写字符")
    private String rfidUuid;

    /** 身份证号，必填，格式校验 18 位 */
    @Schema(description = "身份证号（18 位）", example = "110101199003078888")
    @NotBlank
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式非法")
    private String idCardNo;

    /** 医保卡号，可选 */
    @Schema(description = "医保卡号")
    @Size(max = 64)
    private String insuranceNo;

    /** 本人手机号，可选但建议填写 */
    @Schema(description = "本人手机号（中国大陆 11 位）", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式非法")
    private String phone;

    /** 姓名，必填 */
    @Schema(description = "姓名")
    @NotBlank
    @Size(max = 64)
    private String name;

    /** 性别 */
    @Schema(description = "1男 2女")
    @Min(1)
    @Max(2)
    private Integer gender;

    /** 年龄 */
    @Min(0)
    @Max(200)
    private Integer age;

    /** 病史（长文本） */
    @Schema(description = "病史：慢病、过敏、既往手术等长期健康背景")
    private String medicalHistory;

    /** 紧急联系人姓名 */
    @Schema(description = "紧急联系人姓名")
    @Size(max = 64)
    private String emergencyContactName;

    /** 紧急联系人电话 */
    @Schema(description = "紧急联系人电话")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "紧急联系人电话格式非法")
    private String emergencyContactPhone;
}
