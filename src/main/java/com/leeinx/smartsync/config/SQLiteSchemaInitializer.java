package com.leeinx.smartsync.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.baomidou.mybatisplus.annotation.DbType;

/**
 * SQLite fallback 使用独立建表脚本，避免 MySQL 专用 DDL 语法不兼容。
 */
@Configuration
public class SQLiteSchemaInitializer {

    private static final String SQLITE_SCHEMA_INITIALIZED_BEAN = "sqliteSchemaInitialized";
    private static final String SQL_SESSION_FACTORY_BEAN = "sqlSessionFactory";

    @Bean // 这是一个Bean工厂后置处理区
    public static BeanFactoryPostProcessor sqliteSchemaDependencyPostProcessor() {
        return beanFactory -> {
            //beanFactory 是由Springboot传入的参数, 是用于管理所有Map<key, Object>的容器
            if (beanFactory.containsBeanDefinition(SQL_SESSION_FACTORY_BEAN)) {
                BeanDefinition definition = beanFactory.getBeanDefinition(SQL_SESSION_FACTORY_BEAN);
                Set<String> dependsOn = new LinkedHashSet<>();
                String[] existing = definition.getDependsOn();
                if (existing != null) {
                    dependsOn.addAll(Arrays.asList(existing));
                }
                dependsOn.add(SQLITE_SCHEMA_INITIALIZED_BEAN);
                definition.setDependsOn(dependsOn.toArray(String[]::new));
            }
        };
    }

    @Bean // 依赖注入
    // 检测数据库类型
    public SQLiteSchemaInitialized sqliteSchemaInitialized(DataSource dataSource, DbType dbType) {
        if (dbType == DbType.SQLITE) { // 如果是 SQLITE 的话 使用以下逻辑
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new org.springframework.core.io.ClassPathResource("db/sqlite/schema.sql"));
            populator.setContinueOnError(false);
            DatabasePopulatorUtils.execute(populator, dataSource);
        }
        return new SQLiteSchemaInitialized();
    }

    public static final class SQLiteSchemaInitialized {
    }
}
