package com.leeinx.smartsync.module.terminal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.module.terminal.entity.Terminal;

/** 终端管理服务。注册流程由鉴权模块负责。 */
public interface TerminalService {

    /** 分页查询终端。 */
    IPage<Terminal> page(long current, long size, String keyword);

    /** 按主键查询终端详情。 */
    Terminal getById(Long id);

    /** 修改终端状态：0 待审核，1 启用，2 禁用。 */
    void updateStatus(Long id, Integer status);

    /** 逻辑删除终端。 */
    void delete(Long id);
}
