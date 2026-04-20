package com.leeinx.smartsync.common.exception;

import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.common.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <h2>核心思想</h2>
 * 任何从 Controller 抛出的异常，都不应该让用户直接看到 500 Whitelabel Error Page 或 Spring 的默认 JSON。
 * 这里统一 catch → 转为标准 {@link Result} JSON。
 *
 * <h2>关键注解</h2>
 * <ul>
 *   <li>{@code @RestControllerAdvice} —— 等价于 {@code @ControllerAdvice + @ResponseBody}。
 *       意思是："在所有 Controller 外围织入一层切面，方法返回值自动序列化为 JSON"。</li>
 *   <li>{@code @ExceptionHandler(X.class)} —— 指定当前方法负责处理哪种异常。
 *       Spring 会根据异常类型的<b>最精确匹配</b>来决定走哪个 handler（即 {@code BusinessException} 不会被 {@code Exception} handler 拦截）。</li>
 *   <li>{@code @Slf4j}（Lombok）—— 自动生成 {@code private static final Logger log = LoggerFactory.getLogger(...)}，方便打日志。</li>
 * </ul>
 *
 * <h2>为什么要区分这么多 handler</h2>
 * 不同异常代表不同含义，HTTP 状态码也应该不一样（400/401/403/500），方便运维通过日志/监控定位问题。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常——这是最常见的异常类型。
     * <p>业务异常是"预期内"的错误（比如用户名重复），用 HTTP 200 + 业务 code 返回，让前端根据 {@code Result.code} 判断。</p>
     * <p>注意：这里没有返回 4xx/5xx，因为 HTTP 状态码表示"网络/协议层"是否正常，业务失败不算协议问题。</p>
     *
     * @param e 业务层主动抛出的异常
     * @return 带 Result JSON 的 ResponseEntity
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("BusinessException: code={}, msg={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理 {@code @RequestBody} 的 JSON 参数校验失败。
     * <p>当 Controller 入参用 {@code @Valid @RequestBody DTO} 时，Bean Validation 注解（如 {@code @NotBlank}）
     * 不通过会抛这个异常。</p>
     *
     * @param e Spring 的 MVC 参数校验异常
     * @return HTTP 400 + 所有失败字段拼成的提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST.getCode(), msg));
    }

    /**
     * 处理表单参数（query/form）校验失败。
     * <p>{@link BindException} 是 {@link MethodArgumentNotValidException} 的父类场景，主要用于非 {@code @RequestBody} 的绑定失败。</p>
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST.getCode(), msg));
    }

    /**
     * 处理方法参数 / 路径变量的 Bean Validation 违反（如 {@code @GetMapping} 的 {@code @Min @RequestParam Integer id}）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(Result.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage()));
    }

    /**
     * 处理 Spring Security 认证失败（主要是 JWT 解析失败 / 未带 token）。
     * <p>正常这一类异常会被 {@code SecurityConfig} 里的 {@code AuthenticationEntryPoint} 提前拦截，
     * 这里做兜底：万一有异常漏过 filter chain 也能被正确转成 401。</p>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail(ResultCode.UNAUTHORIZED));
    }

    /**
     * 处理鉴权通过但权限不足的情况（未来会在细化角色后生效）。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccess(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(ResultCode.FORBIDDEN));
    }

    /**
     * 兜底：处理所有未被前面 handler 命中的异常。
     * <p>这类通常是<b>代码 bug</b>：NPE、类型转换失败、SQL 语法错等。日志里打印完整堆栈方便排查，
     * 但返回给前端的是通用"服务器内部错误"，避免暴露实现细节。</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ResultCode.SERVER_ERROR.getCode(), e.getMessage()));
    }
}
