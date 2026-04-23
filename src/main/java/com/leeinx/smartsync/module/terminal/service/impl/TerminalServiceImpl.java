package com.leeinx.smartsync.module.terminal.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;
import com.leeinx.smartsync.module.terminal.entity.Terminal;
import com.leeinx.smartsync.module.terminal.mapper.TerminalMapper;
import com.leeinx.smartsync.module.terminal.service.TerminalService;

import lombok.RequiredArgsConstructor;

/**
 * 终端管理服务实现。
 * 主要处理分页查询、状态修改和逻辑删除。
 */
@Service
@RequiredArgsConstructor // 自动注入
public class TerminalServiceImpl implements TerminalService {

    /** 允许写入的状态值。 */
    private static final Set<Integer> VALID_STATUS = Set.of(0, 1, 2); // 0:正常，1:禁用，2:删除

    private final TerminalMapper terminalMapper;

    /** 分页查询终端，关键词同时匹配编码和名称。 允许管理页面去查询某个或者某些终端*/
    @Override //重写TerminalService里声明的方法
    public IPage<Terminal> page(long current, long size, String keyword) { //要求传入当前页码 返回多少记录 关键词
        Page<Terminal> page = Page.of(current, size); //创建分页对象
        LambdaQueryWrapper<Terminal> q = new LambdaQueryWrapper<>(); //构建查询对象
        if (StringUtils.hasText(keyword)) { // 查询关键词不能为空
            q.like(Terminal::getTerminalCode, keyword) // 匹配内容
                    .or()
                    .like(Terminal::getTerminalName, keyword);
        }
        q.orderByDesc(Terminal::getId); // 按照id降序
        return terminalMapper.selectPage(page, q); //返回分页结果
    }

    /** 查询单个终端，不存在则抛 NOT_FOUND。 */
    @Override
    public Terminal getById(Long id) {
        Terminal t = terminalMapper.selectById(id); //terminalMapper是继承了BaseMapper的, 可以直接用标准的方法查询
        if (t == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return t;
    }

    /** 修改状态前先校验状态值和终端存在性。 */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || !VALID_STATUS.contains(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法状态值");
        }
        Terminal t = getById(id);
        t.setStatus(status);
        terminalMapper.updateById(t);
    }

    /** 逻辑删除终端。 */
    @Override
    public void delete(Long id) {
        getById(id);
        terminalMapper.deleteById(id);
    }
}
