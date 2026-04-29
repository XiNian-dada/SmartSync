package com.leeinx.smartsync;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.annotation.DbType;
import com.leeinx.smartsync.support.SqliteIntegrationTestSupport;

class SQLiteFallbackContextTest extends SqliteIntegrationTestSupport {

    @Autowired
    private DbType dbType;

    @Autowired
    private DataSource dataSource;

    @Test
    void startsWithSqliteFallbackAndInitializesSchema() throws Exception {
        assertThat(dbType).isEqualTo(DbType.SQLITE);

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:sqlite:");
        }

        Integer terminalCount = jdbcTemplate.queryForObject("select count(*) from terminal", Integer.class);
        Integer patientCount = jdbcTemplate.queryForObject("select count(*) from patient", Integer.class);

        assertThat(terminalCount).isZero();
        assertThat(patientCount).isZero();
    }
}
