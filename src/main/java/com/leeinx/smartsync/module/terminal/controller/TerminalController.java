package com.leeinx.smartsync.module.terminal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.common.api.PageResult;
import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.module.terminal.entity.Terminal;
import com.leeinx.smartsync.module.terminal.service.TerminalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 终端管理 Controller。
 *
 * <p>本 Controller 所有路径都需要 JWT（不在 SecurityConfig 白名单里）。</p>
 *
 * <h2>TODO：管理员角色</h2>
 * 当前所有通过 JWT 的终端都能调这些接口（等于"自己管自己"）。后续引入管理员账号体系后，应加
 * {@code @PreAuthorize("hasRole('ADMIN')")} 限制。
 */
@Tag(name = "02. 终端管理")
@RestController
@RequestMapping("/api/terminal")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;

    /**
     * 分页查询终端。
     *
     * @param current 当前页码，默认 1
     * @param size    每页大小，默认 10
     * @param keyword 关键词（可选），模糊匹配 code 或 name
     * @return 分页结果
     */
    @Operation(summary = "分页查询终端")
    @GetMapping("/page")
    public Result<PageResult<Terminal>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        IPage<Terminal> p = terminalService.page(current, size, keyword);
        return Result.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /**
     * 查看终端详情。
     *
     * @param id 路径参数，终端主键
     */
    @Operation(summary = "终端详情")
    @GetMapping("/{id}")
    public Result<Terminal> get(@PathVariable Long id) {
        return Result.ok(terminalService.getById(id));
    }

    /**
     * 管理员审核 / 禁用终端。
     *
     * <p>为什么用 {@code @RequestParam} 而不是 RequestBody：状态修改是单字段操作，用 query 更简单直接。
     * 如果将来要带更多字段（审核意见、操作人），再换成 DTO + RequestBody。</p>
     *
     * @param id     终端主键
     * @param status 新状态：0/1/2
     */
    @Operation(summary = "审核/禁用终端")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "0待审核 1启用 2禁用") @RequestParam Integer status) {
        terminalService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 逻辑删除终端。
     */
    @Operation(summary = "删除终端（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        terminalService.delete(id);
        return Result.ok();
    }
}
