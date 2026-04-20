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

/**
 * 鉴权接口 Controller：终端的注册与登录入口。
 *
 * <h2>核心注解</h2>
 * <ul>
 *   <li>{@code @RestController} —— {@code @Controller + @ResponseBody}，所有方法返回值自动序列化为 JSON。</li>
 *   <li>{@code @RequestMapping("/api/auth")} —— 类级别路径前缀，方法级 {@code @PostMapping("/login")} 拼成完整 {@code /api/auth/login}。</li>
 *   <li>{@code @Tag} —— SpringDoc 注解，在 Swagger 页面上把本 Controller 的接口归组显示。</li>
 *   <li>{@code @Operation} —— 接口在 Swagger 上的描述。</li>
 * </ul>
 *
 * <h2>鉴权豁免</h2>
 * 本 Controller 的路径在 {@link com.leeinx.smartsync.config.SecurityConfig#PUBLIC_PATHS} 白名单里，
 * 可以不带 JWT 访问——否则"还没登录就登录不了"。
 *
 * <h2>{@code @Valid} 的触发时机</h2>
 * Spring MVC 看到入参 {@code @Valid @RequestBody TerminalRegisterDTO} 会先跑 Bean Validation，
 * 校验不通过直接抛 {@link org.springframework.web.bind.MethodArgumentNotValidException}，
 * 被 {@link com.leeinx.smartsync.common.exception.GlobalExceptionHandler} 捕获并返回 400。
 */
@Tag(name = "01. 终端鉴权")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 终端自助注册。
     * <p>注册成功后 status=0（待审核），需要管理员调用 {@code PUT /api/terminal/{id}/status} 切到 1 才能登录。</p>
     *
     * @param dto 注册参数（已通过 @Valid 校验）
     * @return 新建终端的 ID
     */
    @Operation(summary = "终端自助注册（注册后需管理员审核）")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody TerminalRegisterDTO dto) {
        return Result.ok(authService.register(dto));
    }

    /**
     * 终端登录换取 JWT。
     *
     * @param dto 登录参数
     * @return JWT + 过期时间 + 回显终端编码
     */
    @Operation(summary = "终端登录换取 JWT")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody TerminalLoginDTO dto) {
        return Result.ok(authService.login(dto));
    }
}
