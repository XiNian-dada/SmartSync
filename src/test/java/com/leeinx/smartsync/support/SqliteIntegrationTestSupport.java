package com.leeinx.smartsync.support;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * SQLite 集成测试基类。
 * 统一强制走本地 SQLite fallback，避免测试依赖外部 MySQL 环境。
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class SqliteIntegrationTestSupport {

    private static final Path SQLITE_PATH = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "smartsync-test-" + UUID.randomUUID() + ".sqlite");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerSqliteFallbackProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:mysql://127.0.0.1:9/smartsync_test?useSSL=false&connectTimeout=200&socketTimeout=200");
        registry.add("spring.datasource.username", () -> "test");
        registry.add("spring.datasource.password", () -> "test");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("smartsync.datasource.fallback-sqlite.enabled", () -> "true");
        registry.add("smartsync.datasource.fallback-sqlite.path", () -> SQLITE_PATH.toString());
        registry.add("smartsync.datasource.fallback-sqlite.mysql-probe-timeout-ms", () -> "50");
        registry.add("mybatis-plus.configuration.log-impl", () -> "org.apache.ibatis.logging.nologging.NoLoggingImpl");
        registry.add("logging.level.com.leeinx.smartsync", () -> "INFO");
        registry.add("logging.level.org.springframework.security", () -> "WARN");
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM patient");
        jdbcTemplate.update("DELETE FROM terminal");
    }

    protected JsonNode readJson(String content) throws Exception {
        return objectMapper.readTree(content);
    }
}
