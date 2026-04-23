package com.leeinx.smartsync.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 数据源 fallback 配置：优先使用配置中的 MySQL，连不上时回退到本地 SQLite。
 */
@Configuration
public class DataSourceFallbackConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceFallbackConfig.class);

    @Bean
    public DataSourceSelection dataSourceSelection(
            DataSourceProperties properties,
            @Value("${smartsync.datasource.fallback-sqlite.enabled:true}") boolean fallbackEnabled,
            @Value("${smartsync.datasource.fallback-sqlite.path:${user.dir}/data/smartsync.sqlite}") String sqlitePath,
            @Value("${smartsync.datasource.fallback-sqlite.mysql-probe-timeout-ms:1500}") long mysqlProbeTimeoutMs)
            throws SQLException {
        if (canConnect(properties, mysqlProbeTimeoutMs)) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getUrl());
            config.setUsername(properties.getUsername());
            config.setPassword(properties.getPassword());
            config.setDriverClassName(properties.getDriverClassName());
            config.setPoolName("SmartSync-MySQL");
            config.setConnectionTimeout(mysqlProbeTimeoutMs);
            log.info("Using MySQL datasource: {}", properties.getUrl());
            return new DataSourceSelection(new HikariDataSource(config), DbType.MYSQL, false);
        }

        if (!fallbackEnabled) {
            throw new SQLException("MySQL unavailable and SQLite fallback is disabled");
        }

        File sqliteFile = new File(sqlitePath);
        File parent = sqliteFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new SQLException("Failed to create SQLite directory: " + parent.getAbsolutePath());
        }

        HikariConfig sqliteConfig = new HikariConfig();
        sqliteConfig.setJdbcUrl("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
        sqliteConfig.setDriverClassName("org.sqlite.JDBC");
        sqliteConfig.setPoolName("SmartSync-SQLite");
        sqliteConfig.setMaximumPoolSize(1);
        sqliteConfig.setConnectionTestQuery("select 1");
        sqliteConfig.addDataSourceProperty("foreign_keys", "true");
        log.warn("MySQL datasource is unavailable; falling back to SQLite: {}", sqliteFile.getAbsolutePath());
        return new DataSourceSelection(new HikariDataSource(sqliteConfig), DbType.SQLITE, true);
    }

    @Bean
    public DataSource dataSource(DataSourceSelection selection) {
        return selection.dataSource();
    }

    @Bean
    public DbType dbType(DataSourceSelection selection) {
        return selection.dbType();
    }

    private boolean canConnect(DataSourceProperties properties, long timeoutMs) {
        int previousLoginTimeout = DriverManager.getLoginTimeout();
        try {
            String driverClassName = properties.getDriverClassName();
            if (driverClassName != null && !driverClassName.isBlank()) {
                Class.forName(driverClassName);
            }
            DriverManager.setLoginTimeout(Math.max(1, (int) Math.ceil(timeoutMs / 1000.0)));
            try (Connection ignored = DriverManager.getConnection(
                    properties.getUrl(),
                    properties.getUsername(),
                    properties.getPassword())) {
                return true;
            }
        } catch (ClassNotFoundException | SQLException ex) {
            log.debug("MySQL datasource probe failed: {}", ex.getMessage());
            return false;
        } finally {
            DriverManager.setLoginTimeout(previousLoginTimeout);
        }
    }

    public record DataSourceSelection(DataSource dataSource, DbType dbType, boolean sqliteFallback) {
    }
}
