package com.leeinx.smartsync.module.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 患者个人档案实体，对应数据库 {@code patient} 表。
 *
 * <h2>设计边界</h2>
 * SmartSync 只持有"患者这个人"的长期档案（实名信息、联系方式、病史）。
 * 本次挂号、住院、诊断、病房/床位等属于"就诊任务"的数据由医院挂号系统管理，
 * SmartSync 需要时通过接口向其拉取，不重复持久化。
 *
 * <h2>RFID 手环的定位</h2>
 * {@link #rfidUuid} 是<b>临时访问令牌</b>：
 * <ul>
 *   <li>入院时医院分配手环 → 把 UUID 写入这里。</li>
 *   <li>出院回收手环 → 置 null（手环可以再分配给其他患者）。</li>
 *   <li>终端扫到手环 → 通过 UUID 在本表命中患者 → 下发个人档案。</li>
 * </ul>
 *
 * <h2>敏感信息</h2>
 * {@link #idCardNo}、{@link #insuranceNo}、{@link #phone} 在学习项目中明文存储。
 * 真实生产场景应当：
 * <ul>
 *   <li>存储时用 AES 或者 KMS 托管密钥加密（字段级加密）</li>
 *   <li>查询时考虑哈希索引 + 明文比对</li>
 *   <li>返回给前端做脱敏（{@code 110101**********1234}）</li>
 *   <li>访问审计日志必须保留</li>
 * </ul>
 */
@Data
@TableName("patient")
public class Patient implements Serializable {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前绑定手环 12 位 UUID。
     * <p>null 表示该患者档案未持有手环（未入院或手环已回收）。</p>
     */
    private String rfidUuid;

    /**
     * 身份证号。作为患者业务唯一身份，DB 层有唯一索引。
     * <p><b>敏感字段</b>：生产环境必须加密/脱敏。</p>
     */
    private String idCardNo;

    /**
     * 医保卡号。
     * <p><b>敏感字段</b>：生产环境必须加密/脱敏。</p>
     */
    private String insuranceNo;

    /**
     * 患者本人手机号。
     * <p><b>敏感字段</b>：生产环境必须加密/脱敏。</p>
     */
    private String phone;

    /** 姓名 */
    private String name;

    /** 性别：1 男、2 女；允许 null 表示未采集 */
    private Integer gender;

    /**
     * 年龄。
     * <p>学习项目直接存 int。真实场景建议用 {@code birthDate} 字段在展示时动态计算，
     * 避免每年要刷一遍数据。</p>
     */
    private Integer age;

    /**
     * 病史：患者长期性的健康背景（慢病、过敏、既往手术等）。
     * <p>不同于"本次诊断"——诊断属于挂号/就诊信息，由医院挂号系统保存。</p>
     */
    private String medicalHistory;

    /** 紧急联系人姓名 */
    private String emergencyContactName;

    /**
     * 紧急联系人电话。
     * <p><b>敏感字段</b>：虽然不是本人手机号，也应遵循相同脱敏/加密策略。</p>
     */
    private String emergencyContactPhone;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
