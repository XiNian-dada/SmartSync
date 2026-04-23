package com.leeinx.smartsync.common.exception;

import com.leeinx.smartsync.common.api.ResultCode;

import lombok.Getter;

/**
 * 业务层主动抛出的异常。
 * 这类异常会被统一转换成标准 JSON 响应，而不是直接返回 500 页面。
 * 也就是说, 这些异常其实是逻辑错误, 比如某些调用的错误, 参数错误等
 * 是由项目主动返回的, 返回给 GlobalExceptionHandler 统一处理合成 JSON。
 * 
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码，对应 {@link ResultCode#getCode()}。 */
    private final Integer code;

    /** 使用预定义状态码和默认消息。 */
    public BusinessException(ResultCode rc) {
        super(rc.getMessage()); //父类是RuntimeException 把错误信息丢给父类
        this.code = rc.getCode(); //把错误码赋给code
    }

    /** 使用预定义状态码，并覆盖默认消息。 */
    public BusinessException(ResultCode rc, String message) {
        super(message); // 如果要指定message吧
        this.code = rc.getCode();
    }

    /** 完全自定义状态码和消息。 */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code; // 如果要指定code和message
    }
}
