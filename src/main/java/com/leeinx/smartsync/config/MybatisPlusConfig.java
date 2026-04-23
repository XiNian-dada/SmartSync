package com.leeinx.smartsync.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

/**
 * MyBatis-Plus 统一配置。
 * 使用 `@MapperScan` 扫描所有业务模块下的 mapper 包。
 * 用来管理modele下的所有内容
 */
@Configuration
@MapperScan("com.leeinx.smartsync.module.*.mapper")
public class MybatisPlusConfig {

    /**
     * 注册分页和乐观锁拦截器。
     * 当前项目主要依赖分页拦截器，乐观锁先预留。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(ObjectProvider<DbType> dbTypeProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 新建一个interceptor的容器
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(dbTypeProvider.getIfAvailable(() -> DbType.MYSQL)); // 新建一个分页拦截器
        // 防止前端一次性拉取过大的分页数据。
        page.setMaxLimit(500L); // 设置最大可取的页面数
        interceptor.addInnerInterceptor(page); // 把这个分页拦截器添加到interceptor容器里
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor()); // 添加乐观锁拦截器
        /**
         * 乐观锁:
         * 通过假设业务不会出现县线程冲突, 以此提高项目的查询效率和并发性能
         * 当然, 出现意外的情况, 就需要额外的处理了
         */
        return interceptor; //返回这个容器
    }
}
