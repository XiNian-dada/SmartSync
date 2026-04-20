package com.leeinx.smartsync.module.terminal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.module.terminal.entity.Terminal;

/**
 * 终端管理 Service 接口：对应管理员对终端的增删改查操作。
 *
 * <p>注意：<b>注册流程</b>不在本接口（在 {@link com.leeinx.smartsync.module.auth.service.AuthService#register}），
 * 因为注册是"匿名可访问"的特殊操作，语义上属于鉴权模块。</p>
 */
public interface TerminalService {

    /**
     * 分页查询终端。
     *
     * @param current 当前页（从 1 开始）
     * @param size    每页大小
     * @param keyword 搜索关键词，模糊匹配 terminal_code 或 terminal_name；可为 null
     * @return MyBatis-Plus 的分页对象（包含 records、total、current、size 等）
     */
    IPage<Terminal> page(long current, long size, String keyword);

    /**
     * 按主键查终端详情。
     *
     * @param id 主键
     * @return 终端实体
     * @throws com.leeinx.smartsync.common.exception.BusinessException 不存在时抛 NOT_FOUND
     */
    Terminal getById(Long id);

    /**
     * 修改终端状态（审核 / 禁用）。
     *
     * @param id     主键
     * @param status 0 待审核、1 启用、2 禁用
     * @throws com.leeinx.smartsync.common.exception.BusinessException 状态值非法 / 终端不存在
     */
    void updateStatus(Long id, Integer status);

    /**
     * 逻辑删除终端。
     * <p>注意：数据库中记录仍保留（仅 {@code deleted=1}），方便历史追溯。</p>
     *
     * @param id 主键
     * @throws com.leeinx.smartsync.common.exception.BusinessException 终端不存在
     */
    void delete(Long id);
}
