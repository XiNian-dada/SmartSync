package com.leeinx.smartsync.config;

import java.time.LocalDateTime;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

/**
 * 统一填充审计字段。
 * 目前负责 `createdAt` 和 `updatedAt` 的自动写入。
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /** 新增时同时写入创建时间和更新时间。 */
    // 这样可以让操作时, 自动的把时间戳信息写入数据库, 不需要每次都显式指定。
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    /** 更新时只刷新更新时间。 */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
