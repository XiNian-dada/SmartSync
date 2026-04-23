package com.leeinx.smartsync.module.terminal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leeinx.smartsync.module.terminal.entity.Terminal;

/** Terminal 表的数据访问层。当前直接复用 BaseMapper 提供的 CRUD。 */
public interface TerminalMapper extends BaseMapper<Terminal> {
}
