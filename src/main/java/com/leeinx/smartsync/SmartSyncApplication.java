package com.leeinx.smartsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartSync 应用启动类。
 *
 * <h2>作用</h2>
 * 作为 Spring Boot 应用的主入口，负责引导整个应用上下文（ApplicationContext）启动。
 *
 * <h2>核心注解说明</h2>
 * <ul>
 *   <li>{@code @SpringBootApplication} —— 这是一个"组合注解"，等价于同时标注：
 *     <ul>
 *       <li>{@code @SpringBootConfiguration} ：把本类声明为一个 Spring 配置类，可在里面定义 {@code @Bean}</li>
 *       <li>{@code @EnableAutoConfiguration} ：启用 Spring Boot 的自动装配（根据 classpath 自动配置 DataSource、MVC、Security 等）</li>
 *       <li>{@code @ComponentScan} ：从本类所在包开始扫描 {@code @Component @Service @Controller @Repository @Configuration} 等注解的类</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>扫描范围提示</h2>
 * 本类位于 {@code com.leeinx.smartsync} 包下，因此 Spring 会扫描它所有的子包（common、config、security、module 等），
 * 确保所有业务 Bean 都能被自动注册到容器中。
 */
@SpringBootApplication
public class SmartSyncApplication {

    /**
     * JVM 入口方法。
     * <p>
     * 调用 {@link SpringApplication#run(Class, String...)} 会完成：
     * <ol>
     *   <li>创建 {@link org.springframework.context.ApplicationContext Spring 容器}</li>
     *   <li>执行自动装配（根据 starter 依赖推断需要的 Bean）</li>
     *   <li>启动内嵌 Tomcat，监听 {@code server.port} 配置的端口（默认 8080）</li>
     *   <li>触发 {@link org.springframework.boot.ApplicationRunner} / {@link org.springframework.boot.CommandLineRunner} 回调</li>
     * </ol>
     *
     * @param args 命令行参数，会透传到 Spring 的 {@link org.springframework.boot.ApplicationArguments}，
     *             可被任意 Bean 通过注入 {@code ApplicationArguments} 读取（例如启动参数 {@code --server.port=9090}）
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartSyncApplication.class, args);
    }

}
