package com.leeinx.smartsync.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器。
 *
 * <h2>解决什么问题</h2>
 * 几乎所有业务表都有 {@code created_at}、{@code updated_at} 这类审计字段。
 * 如果手动在每个 Service 里 {@code entity.setCreatedAt(LocalDateTime.now())}，会到处重复，还容易漏掉。
 * <p>MyBatis-Plus 提供 {@link MetaObjectHandler} 机制：在实体类字段上标 {@code @TableField(fill = INSERT)}，
 * 框架在执行 insert/update 前会回调本类的方法填充值。</p>
 *
 * <h2>配合的实体注解</h2>
 * 见 {@link com.leeinx.smartsync.module.terminal.entity.Terminal} 的 {@code createdAt/updatedAt} 字段。
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * insert 语句执行前被调用：同时填充创建时间和更新时间。
     * <p>{@code strictInsertFill} 方法会检查字段是否存在于实体类，且仅在字段值为 null 时填充（允许手动覆盖）。</p>
     *
     * @param metaObject MyBatis 反射包装的实体对象，可读写字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    /**
     * update 语句执行前被调用：仅刷新更新时间，创建时间保留原值不动。
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
