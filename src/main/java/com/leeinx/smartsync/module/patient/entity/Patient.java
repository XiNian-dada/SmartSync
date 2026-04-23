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
 * 患者档案实体，对应 `patient` 表。
 * 这里保存的是长期档案，不包含挂号、住院和诊断等一次性就诊数据。
 */
@Data
@TableName("patient")
public class Patient implements Serializable {

    /** 主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 当前绑定手环的 12 位 UUID，空表示未绑定。 */
    private String rfidUuid;

    /** 身份证号，业务唯一。 */
    private String idCardNo;

    /** 医保卡号。 */
    private String insuranceNo;

    /** 患者本人手机号。 */
    private String phone;

    /** 姓名。 */
    private String name;

    /** 性别：1 男，2 女；可为空。 */
    private Integer gender;

    /** 年龄。 */
    private Integer age;

    /** 长期病史信息。 */
    private String medicalHistory;

    /** 紧急联系人姓名。 */
    private String emergencyContactName;

    /** 紧急联系人电话。 */
    private String emergencyContactPhone;

    /** 创建时间。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记。 */
    @TableLogic
    private Integer deleted;
}
