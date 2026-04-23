package com.leeinx.smartsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartSync 启动入口。
 * Spring Boot 会从当前包开始扫描配置、控制器、服务和安全组件。
 */
@SpringBootApplication
public class SmartSyncApplication {

    /**
     * 启动 Spring 容器并注册整个应用。
     * SpringBoot能够扫描包下的所有Java文件, 所以这里不需要怎么动他
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartSyncApplication.class, args);
    }

}
