package com.leeinx.smartsync.module.terminal.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.common.api.PageResult;
import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.module.terminal.entity.Terminal;
import com.leeinx.smartsync.module.terminal.service.TerminalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 终端管理接口。
 * 当前只要求登录，后续可继续细分为管理员权限。
 */
@Tag(name = "02. 终端管理") // 用于让Swagger生成文档
@RestController //声明他是RestController 返回值会被自动构建为JSON
@RequestMapping("/api/terminal") // API 请求地址
@RequiredArgsConstructor // 构造函数注入
public class TerminalController {

    private final TerminalService terminalService;

    /** 分页查询终端。 */
    @Operation(summary = "分页查询终端") // 让文档生成关于这个接口的文档
    @GetMapping("/page") // 请求地址

    public Result<PageResult<Terminal>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        IPage<Terminal> p = terminalService.page(current, size, keyword);
        return Result.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 查询终端详情。 */
    @Operation(summary = "终端详情")
    @GetMapping("/{id}")
    public Result<Terminal> get(@PathVariable Long id) {
        return Result.ok(terminalService.getById(id));
    }

    /** 修改终端状态。 */
    @Operation(summary = "审核/禁用终端")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Parameter(description = "0待审核 1启用 2禁用") @RequestParam Integer status) {
        terminalService.updateStatus(id, status);
        return Result.ok();
    }

    /** 逻辑删除终端。 */
    @Operation(summary = "删除终端（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        terminalService.delete(id);
        return Result.ok();
    }
}
