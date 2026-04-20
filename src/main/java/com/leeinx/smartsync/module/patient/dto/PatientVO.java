package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下发给终端的患者信息视图。
 *
 * <h2>为什么不直接返回 {@link com.leeinx.smartsync.module.patient.entity.Patient}</h2>
 * <ul>
 *   <li>Entity 有 {@code deleted}、{@code createdAt} 等技术字段，终端不关心。</li>
 *   <li>VO 按"终端需要什么"精确定义字段，未来加内部字段不会影响下发结构。</li>
 *   <li>避免 Lazy Loading 等 ORM 副作用（如果切 JPA 时更关键）。</li>
 * </ul>
 */
@Data
@Schema(description = "患者信息（下发终端）")
public class PatientVO {
    /** 患者主键 */
    private Long id;
    /** 12 位手环 UUID */
    private String rfidUuid;
    /** 姓名 */
    private String name;
    /** 性别 */
    private Integer gender;
    /** 年龄 */
    private Integer age;
    /** 病历号 */
    private String medicalRecordNo;
    /** 病区 */
    private String ward;
    /** 床号 */
    private String bedNo;
    /** 诊断摘要 */
    private String diagnosis;
    /** 入院时间 */
    private LocalDateTime admissionAt;
    /** 状态：0 出院 / 1 在院 */
    private Integer status;
}
