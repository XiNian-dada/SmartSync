package com.leeinx.smartsync.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * Flyway 当前只迁移 MySQL；SQLite fallback 由 SQLiteSchemaInitializer 初始化。
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(DbType dbType) {
        return flyway -> {
            if (dbType == DbType.MYSQL) {
                flyway.migrate();
            }
        };
    }
}
