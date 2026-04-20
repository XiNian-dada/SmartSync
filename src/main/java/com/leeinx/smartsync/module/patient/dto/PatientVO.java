package com.leeinx.smartsync.module.patient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 下发给终端的患者信息视图。
 *
 * <h2>和 Entity 的差异</h2>
 * <ul>
 *   <li>不含技术字段：{@code createdAt / updatedAt / deleted}</li>
 *   <li>其它字段与 Entity 一致，便于终端直接展示</li>
 * </ul>
 *
 * <h2>敏感字段脱敏 TODO</h2>
 * 当前 {@code idCardNo / phone / insuranceNo / emergencyContactPhone} 原样下发。
 * 生产环境应在 Service → VO 转换时做脱敏（如 {@code 138****8000}）。
 */
@Data
@Schema(description = "患者信息（下发终端）")
public class PatientVO {
    /** 患者主键 */
    private Long id;
    /** 当前手环 UUID */
    private String rfidUuid;
    /** 身份证号 */
    private String idCardNo;
    /** 医保卡号 */
    private String insuranceNo;
    /** 本人手机号 */
    private String phone;
    /** 姓名 */
    private String name;
    /** 性别：1 男 / 2 女 */
    private Integer gender;
    /** 年龄 */
    private Integer age;
    /** 病史 */
    private String medicalHistory;
    /** 紧急联系人姓名 */
    private String emergencyContactName;
    /** 紧急联系人电话 */
    private String emergencyContactPhone;
}
