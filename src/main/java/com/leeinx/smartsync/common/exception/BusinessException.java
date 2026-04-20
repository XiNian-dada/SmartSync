package com.leeinx.smartsync.common.exception;

import com.leeinx.smartsync.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * <h2>为什么要自定义异常</h2>
 * <p>Java 约定：非预期的代码 bug 抛 {@link RuntimeException}，而<b>可预期的业务错误</b>（比如"用户名已存在""余额不足"）
 * 应该用一种专门的异常类型来表达。这样：</p>
 * <ol>
 *   <li>Service 层可以直接 {@code throw new BusinessException(...)} 中断当前流程，事务会回滚。</li>
 *   <li>{@link GlobalExceptionHandler} 只要 catch {@code BusinessException}，就知道这是"业务问题"而不是"程序出 bug"，
 *       可以安全地把消息原文返回给前端（而不用担心泄漏堆栈信息）。</li>
 * </ol>
 *
 * <h2>为什么继承 {@link RuntimeException} 而不是 {@code Exception}</h2>
 * <p>{@code Exception} 是受检异常（checked），必须在方法签名上声明 {@code throws}，或者用 try-catch 处理，代码会很啰嗦。
 * {@code RuntimeException} 是非受检（unchecked），调用方不必关心，让 Spring AOP / GlobalExceptionHandler 来统一接管。</p>
 *
 * <h2>Lombok {@code @Getter}</h2>
 * 为 {@code code} 字段自动生成 {@code getCode()}。异常的 {@code message} 由父类 {@code RuntimeException} 处理（通过 {@code getMessage()}）。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码，对应 {@link ResultCode#getCode()} */
    private final Integer code;

    /**
     * 用预定义的 {@link ResultCode} 抛异常。这是最常用的构造方式，消息就用枚举里的默认文案。
     * <p>示例：{@code throw new BusinessException(ResultCode.TERMINAL_CODE_EXISTS);}</p>
     *
     * @param rc 预定义状态码
     */
    public BusinessException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    /**
     * 用预定义状态码 + 自定义消息抛异常。
     * <p>当同一类错误需要携带不同的上下文（比如"RFID 已绑定给患者 #{id}"）时使用。</p>
     *
     * @param rc      状态码（提供 code）
     * @param message 自定义消息，覆盖 {@link ResultCode#getMessage()}
     */
    public BusinessException(ResultCode rc, String message) {
        super(message);
        this.code = rc.getCode();
    }

    /**
     * 完全自定义：状态码和消息都由调用方决定。
     * <p>一般极少使用，优先考虑在 {@link ResultCode} 里加一个新枚举值。</p>
     *
     * @param code    业务状态码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
