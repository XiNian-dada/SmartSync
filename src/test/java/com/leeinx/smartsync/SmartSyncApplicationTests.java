package com.leeinx.smartsync;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 上下文集成测试。
 * 当前依赖本地 MySQL，因此默认禁用。
 */
@Disabled("需要本地 MySQL 环境，移除此注解以启用集成测试")
@SpringBootTest
class SmartSyncApplicationTests {

    /** 只要容器能启动，这个测试就算通过。 */
    @Test
    void contextLoads() {
    }

}
