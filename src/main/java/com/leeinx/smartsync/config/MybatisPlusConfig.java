package com.leeinx.smartsync.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 *
 * <h2>{@code @MapperScan} 的作用</h2>
 * 告诉 MyBatis "哪些包下的接口是 Mapper 接口，需要在启动时扫描并自动生成代理实现"。
 * <p>
 * 这里用通配符 {@code module.*.mapper}，匹配 {@code module/terminal/mapper}、{@code module/patient/mapper} 等。
 * 这样每加一个业务模块，Mapper 不用额外注册，只要放在 {@code module/<新模块>/mapper} 目录下即可被自动扫描。
 * </p>
 *
 * <h2>为什么没用 {@code @Mapper} 注解到每个 Mapper 接口上</h2>
 * 两种方式任选其一：
 * <ul>
 *   <li>集中式：本文件用 {@code @MapperScan}，业务 Mapper 接口保持干净（不用任何注解）。</li>
 *   <li>分散式：每个 Mapper 加 {@code @Mapper}，不用本配置，但业务包多了以后漏加容易出 bug。</li>
 * </ul>
 * 本项目选集中式，更安全。
 */
@Configuration
@MapperScan("com.leeinx.smartsync.module.*.mapper")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 核心拦截器，并挂载两个常用内部拦截器。
     *
     * <h3>{@link PaginationInnerInterceptor} —— 分页拦截器</h3>
     * <p>没有这个拦截器时，调用 {@code mapper.selectPage(page, wrapper)} 不会真正分页，会查全表再内存截断。
     * 挂上后，MyBatis-Plus 会在 SQL 执行前自动改写为 {@code LIMIT offset,size}（MySQL 方言）。</p>
     * <p>{@code setMaxLimit(500)} 保护性设置：即使前端传 {@code size=10000}，最多也只会查 500 条，避免意外打爆数据库。</p>
     *
     * <h3>{@link OptimisticLockerInnerInterceptor} —— 乐观锁拦截器</h3>
     * <p>如果实体类上有 {@code @Version} 注解的字段，更新时会自动拼接 {@code WHERE version=?} 并 version+1，
     * 实现"多人同时编辑同一条记录时后写覆盖前写"的检测。本项目暂未使用 {@code @Version}，但先挂着备用。</p>
     *
     * @return 完整配置好的 MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        page.setMaxLimit(500L);
        interceptor.addInnerInterceptor(page);
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
