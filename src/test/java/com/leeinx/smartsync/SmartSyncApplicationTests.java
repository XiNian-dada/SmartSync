package com.leeinx.smartsync;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 上下文加载测试。
 *
 * <h2>{@code @SpringBootTest} 做什么</h2>
 * 启动一个<b>完整</b>的 Spring 容器：扫描所有 Bean、建立 DataSource、跑 Flyway 迁移……
 * 能通过说明"所有配置是对的、所有 Bean 能正确注入"。这是最廉价又最有效的集成测试。
 *
 * <h2>为什么被 {@code @Disabled}</h2>
 * 需要连接真实 MySQL 才能启动成功。CI 环境或新同事拉取代码时可能没有 MySQL，
 * 默认禁用避免"我什么都没改就报错"的困扰。本地准备好环境后手动去掉即可。
 *
 * <h2>JUnit 5 语法提示</h2>
 * <ul>
 *   <li>{@code @Test} —— 标注一个测试方法</li>
 *   <li>{@code @Disabled} —— 跳过该测试（可加字符串说明原因）</li>
 *   <li>类和方法都不需要 {@code public}（JUnit 5 改进）</li>
 * </ul>
 */
@Disabled("需要本地 MySQL 环境，移除此注解以启用集成测试")
@SpringBootTest
class SmartSyncApplicationTests {

    /**
     * 空方法体也能作为有效测试——{@code @SpringBootTest} 会在测试执行前启动上下文，
     * 启动失败（找不到 Bean、配置错误、数据库连不上）JUnit 会标记为失败。
     */
    @Test
    void contextLoads() {
    }

}
