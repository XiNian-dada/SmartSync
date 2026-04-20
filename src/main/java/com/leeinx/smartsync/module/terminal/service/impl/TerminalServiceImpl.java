package com.leeinx.smartsync.module.terminal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;
import com.leeinx.smartsync.module.terminal.entity.Terminal;
import com.leeinx.smartsync.module.terminal.mapper.TerminalMapper;
import com.leeinx.smartsync.module.terminal.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 终端管理 Service 实现。
 *
 * <h2>业务规则</h2>
 * <ul>
 *   <li>状态值约束：只能是 {@link #VALID_STATUS} 里的 {@code 0/1/2}。</li>
 *   <li>查询时关键词同时匹配 code 和 name（OR 条件）。</li>
 *   <li>所有"按 id 操作"的方法都先调 {@link #getById(Long)} 校验存在性，失败则抛 NOT_FOUND。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

    /** 合法状态值白名单，使用 {@link Set#of} 创建不可变集合 */
    private static final Set<Integer> VALID_STATUS = Set.of(0, 1, 2);

    private final TerminalMapper terminalMapper;

    /**
     * 分页查询。
     *
     * <h3>LambdaQueryWrapper 用法</h3>
     * 支持链式拼 WHERE 条件：{@code .eq / .like / .in / .between ...}。
     * 用方法引用（{@code Terminal::getTerminalCode}）代替字符串列名，编译期发现字段重命名。
     */
    @Override
    public IPage<Terminal> page(long current, long size, String keyword) {
        Page<Terminal> page = Page.of(current, size);
        LambdaQueryWrapper<Terminal> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // .or() 之后的条件整体被包在 OR 里：WHERE code LIKE ? OR name LIKE ?
            q.like(Terminal::getTerminalCode, keyword)
                    .or()
                    .like(Terminal::getTerminalName, keyword);
        }
        // 按 id 倒序：最新注册的在最前
        q.orderByDesc(Terminal::getId);
        return terminalMapper.selectPage(page, q);
    }

    /**
     * 按 id 查询单条。
     *
     * @throws BusinessException 不存在时抛 {@link ResultCode#NOT_FOUND}
     */
    @Override
    public Terminal getById(Long id) {
        Terminal t = terminalMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return t;
    }

    /**
     * 修改状态。
     * <p>先校验状态值合法，再确认终端存在，最后 updateById。</p>
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法状态值");
        }
        Terminal t = getById(id);
        t.setStatus(status);
        terminalMapper.updateById(t);
    }

    /**
     * 逻辑删除：先确认终端存在再调 deleteById（实际产生 {@code UPDATE ... SET deleted=1}）。
     */
    @Override
    public void delete(Long id) {
        getById(id);
        terminalMapper.deleteById(id);
    }
}
