package com.leeinx.smartsync.module.auth.controller;

import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.module.auth.dto.LoginVO;
import com.leeinx.smartsync.module.auth.dto.TerminalLoginDTO;
import com.leeinx.smartsync.module.auth.dto.TerminalRegisterDTO;
import com.leeinx.smartsync.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 终端注册与登录入口。 */
@Tag(name = "01. 终端鉴权")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 注册后默认进入待审核状态。 */
    @Operation(summary = "终端自助注册（注册后需管理员审核）")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody TerminalRegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    /** 登录成功后返回 JWT。 */
    @Operation(summary = "终端登录换取 JWT")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody TerminalLoginDTO dto) {
        return Result.ok(authService.login(dto));
    }
}
