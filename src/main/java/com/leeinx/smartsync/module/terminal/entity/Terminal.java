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

/**
 * 终端实体：与数据库 {@code terminal} 表一一对应。
 *
 * <h2>MyBatis-Plus 核心注解</h2>
 * <ul>
 *   <li>{@code @TableName("terminal")} —— 指定对应的表名。不写的话默认用类名的小写蛇形。</li>
 *   <li>{@code @TableId(type = IdType.AUTO)} —— 声明主键字段 + 生成策略；
 *       {@code AUTO} 表示用 MySQL 的 AUTO_INCREMENT，insert 后主键会被回填。</li>
 *   <li>{@code @TableField(fill = FieldFill.INSERT)} —— 告诉框架在插入时自动填充此字段，
 *       实际填充逻辑在 {@link com.leeinx.smartsync.config.MybatisMetaObjectHandler}。</li>
 *   <li>{@code @TableLogic} —— 逻辑删除字段。删除时不 {@code DELETE FROM}，而是 {@code UPDATE deleted=1}；
 *       所有查询默认追加 {@code WHERE deleted=0}。</li>
 * </ul>
 *
 * <h2>命名约定：驼峰 ↔ 下划线</h2>
 * 字段用 Java 驼峰（{@code terminalCode}），数据库列用下划线（{@code terminal_code}）——
 * 由 {@code mybatis-plus.configuration.map-underscore-to-camel-case} 配置自动转换。
 *
 * <h2>{@link Serializable}</h2>
 * MyBatis-Plus 源码推荐实体实现 Serializable，便于缓存、分布式场景序列化。
 */
@Data
@TableName("terminal")
public class Terminal implements Serializable {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 终端编码，登录账号，业务唯一 */
    private String terminalCode;

    /** 终端名称，用于管理页显示 */
    private String terminalName;

    /** 密钥的 BCrypt 哈希值（不明文）。注意字段名是 {@code secretHash} 而不是 {@code password} —— 让读代码的人立刻知道这不是明文。 */
    private String secretHash;

    /** 状态：0 待审核、1 启用、2 禁用。只有 1 才能登录成功。 */
    private Integer status;

    /** 最近一次登录成功的时间，登录接口里更新 */
    private LocalDateTime lastLoginAt;

    /** 创建时间，由 {@link com.leeinx.smartsync.config.MybatisMetaObjectHandler} 在 insert 时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，insert 和 update 时都自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常、1 已删除。{@link TableLogic} 让 MyBatis-Plus 自动处理。 */
    @TableLogic
    private Integer deleted;
}
