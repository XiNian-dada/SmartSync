package com.leeinx.smartsync.module.terminal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 终端实体，对应 `terminal` 表。 */
@Data
@TableName("terminal")
public class Terminal implements Serializable {

    /** 主键。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 终端编码，业务唯一。 */
    private String terminalCode;

    /** 终端名称。 */
    private String terminalName;

    /** 终端密钥的 BCrypt 哈希值。 */
    private String secretHash;

    /** 状态：0 待审核，1 启用，2 禁用。 */
    private Integer status;

    /** 最近一次登录时间。 */
    private LocalDateTime lastLoginAt;

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
