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
 * 患者实体：与数据库 {@code patient} 表对应。
 *
 * <h2>与 RFID 的关系</h2>
 * {@code rfidUuid} 是<b>当前手环</b>绑定的 12 位 UUID：
 * <ul>
 *   <li>入院时绑定一个手环 → rfidUuid 写入该 UUID。</li>
 *   <li>出院时手环回收 → rfidUuid 清空（置 null）。</li>
 *   <li>同一患者下次住院可能分配新手环 → rfidUuid 更新。</li>
 * </ul>
 * 数据库通过 {@code UNIQUE (rfid_uuid, deleted)} 保证"同一时刻同一 UUID 只能绑定一个在院患者"。
 *
 * <h2>为什么 {@code rfidUuid} 不是 NOT NULL</h2>
 * 患者出院后字段置空是合法业务状态；MySQL 对 NULL 的唯一约束允许多行 NULL 共存，刚好满足需求。
 */
@Data
@TableName("patient")
public class Patient implements Serializable {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前绑定手环 12 位 UUID，出院时置空。
     * <p>不存 13 位完整 RFID，因为校验位可以随时从 UUID + 服务器密钥重算，没必要持久化。</p>
     */
    private String rfidUuid;

    /** 患者姓名 */
    private String name;

    /** 性别：1 男、2 女（允许 null 表示未采集） */
    private Integer gender;

    /** 年龄 */
    private Integer age;

    /** 病历号，医院内唯一 */
    private String medicalRecordNo;

    /** 病区（如"心内科 3 楼"） */
    private String ward;

    /** 床号 */
    private String bedNo;

    /** 诊断摘要，{@code TEXT} 类型存储长文本 */
    private String diagnosis;

    /** 入院时间 */
    private LocalDateTime admissionAt;

    /** 出院时间，在院时为 null */
    private LocalDateTime dischargeAt;

    /** 状态：0 出院、1 在院 */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
