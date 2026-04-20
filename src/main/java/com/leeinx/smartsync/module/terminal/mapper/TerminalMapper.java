package com.leeinx.smartsync.module.terminal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leeinx.smartsync.module.terminal.entity.Terminal;

/**
 * Terminal 表的数据访问层（DAO）。
 *
 * <h2>为什么是一个空接口</h2>
 * 继承 {@link BaseMapper} 就自动获得了 CRUD 方法：
 * <ul>
 *   <li>{@code insert(T)} / {@code updateById(T)} / {@code deleteById(id)}</li>
 *   <li>{@code selectById(id)} / {@code selectOne(wrapper)} / {@code selectList(wrapper)}</li>
 *   <li>{@code selectPage(page, wrapper)} ——分页查询（需要 {@link com.leeinx.smartsync.config.MybatisPlusConfig} 里注册分页拦截器）</li>
 * </ul>
 * 本项目目前对 terminal 表没有复杂 SQL 需求，留空接口即可。
 *
 * <h2>如果要写复杂 SQL</h2>
 * 两种方式：
 * <ol>
 *   <li>加 {@code @Select("SQL")} 方法</li>
 *   <li>在 {@code resources/mapper/} 下建对应 XML（application.yaml 中的 {@code mapper-locations} 会扫描）</li>
 * </ol>
 */
public interface TerminalMapper extends BaseMapper<Terminal> {
}
