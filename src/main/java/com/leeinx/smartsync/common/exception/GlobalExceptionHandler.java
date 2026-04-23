package com.leeinx.smartsync.common.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.common.api.ResultCode;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一异常出口。
 * 目标是把 Controller 层抛出的异常稳定地转成 Result JSON。
 * 
 * 这个会收集所有的错误信息, 统一为 JSON 值返回给前端处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务规则冲突，HTTP 层仍返回 200，细节放在业务 code 中。 */
    @ExceptionHandler(BusinessException.class) // 注解: 专门处理BusinessException的业务错误
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("BusinessException: code={}, msg={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** 处理 JSON 请求体校验失败。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST.getCode(), msg));
    }

    /** 处理 query/form 参数绑定失败。 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ResultCode.BAD_REQUEST.getCode(), msg));
    }

    /** 处理方法参数级别的 Bean Validation 异常。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(Result.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage()));
    }

    /** 兜底处理认证失败。 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail(ResultCode.UNAUTHORIZED));
    }

    /** 处理已登录但权限不足的情况。 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccess(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(ResultCode.FORBIDDEN));
    }

    /** 兜底处理未预期异常，日志保留堆栈，响应对外只暴露通用错误。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ResultCode.SERVER_ERROR.getCode(), e.getMessage()));
    }
}
