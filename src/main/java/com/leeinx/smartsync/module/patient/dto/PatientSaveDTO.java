package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者新建 / 更新的共用入参。
 *
 * <h2>为什么创建和更新共用一个 DTO</h2>
 * <ul>
 *   <li>字段大部分一样，分开两个类会重复。</li>
 *   <li>创建和更新的约束也一致（除了主键不由客户端传，由路径参数 {@code /{id}} 传）。</li>
 * </ul>
 * 将来如果有差异（如"更新时不允许改手环"），再拆成 {@code PatientCreateDTO} / {@code PatientUpdateDTO}。
 *
 * <h2>Bean Validation 注解</h2>
 * <ul>
 *   <li>{@code @Pattern} ——用正则校验 RFID UUID 是 12 位 Base32 字符（{@code [0-9A-V]}）。</li>
 *   <li>{@code @NotBlank} ——字符串非 null、非空白。</li>
 *   <li>{@code @Min / @Max} ——数值上下界。</li>
 *   <li>{@code @Size} ——字符串长度范围。</li>
 * </ul>
 */
@Data
@Schema(description = "患者新建/更新")
public class PatientSaveDTO {

    /**
     * 当前手环 12 位 UUID。
     * <p>允许 null（出院 / 暂未分配手环）；若填则必须符合 Base32 大写字符格式。</p>
     */
    @Schema(description = "当前手环 12 位 UUID（Base32 大写字符），出院时可置空")
    @Pattern(regexp = "^[0-9A-V]{12}$", message = "rfidUuid 必须是 12 位 Base32 大写字符")
    private String rfidUuid;

    /** 姓名，必填 */
    @Schema(description = "姓名")
    @NotBlank
    @Size(max = 64)
    private String name;

    /** 性别，1 男 2 女 */
    @Schema(description = "1男 2女")
    @Min(1)
    @Max(2)
    private Integer gender;

    /** 年龄，合理范围 0-200 */
    @Min(0)
    @Max(200)
    private Integer age;

    /** 病历号 */
    @Size(max = 64)
    private String medicalRecordNo;

    /** 病区 */
    @Size(max = 64)
    private String ward;

    /** 床号 */
    @Size(max = 32)
    private String bedNo;

    /** 诊断 */
    private String diagnosis;

    /** 入院时间 */
    private LocalDateTime admissionAt;

    /** 出院时间 */
    private LocalDateTime dischargeAt;

    /** 状态：0 出院 / 1 在院 */
    @Schema(description = "0出院 1在院")
    @Min(0)
    @Max(1)
    private Integer status;
}
