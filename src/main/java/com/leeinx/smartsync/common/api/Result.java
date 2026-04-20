package com.leeinx.smartsync.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应包装类。
 *
 * <h2>为什么要有统一响应</h2>
 * 前端/终端调用任何接口，都能拿到相同结构的 JSON：{@code {code, message, data}}。
 * 这样前端只要判断 {@code code} 是否 200 即可，不需要按接口单独判断业务状态。
 *
 * <h2>关键点</h2>
 * <ul>
 *   <li>{@link JsonInclude}({@link JsonInclude.Include#NON_NULL}) —— Jackson 序列化时，值为 null 的字段不写入 JSON，
 *       例如没有 data 的场景响应会是 {@code {"code":200,"message":"OK"}} 而不是 {@code {"code":200,"message":"OK","data":null}}。</li>
 *   <li>{@link Serializable} —— 便于在分布式场景（如 Redis 缓存、MQ 消息）中被序列化传输。</li>
 *   <li>泛型 {@code <T>} —— 让 data 字段可以是任意类型（Long、VO、List 等）。</li>
 * </ul>
 *
 * <h2>静态工厂方法的意义</h2>
 * 对外只暴露 {@code ok()/fail()} 而不是直接 new + setter：
 * <ol>
 *   <li>调用方代码更语义化：{@code Result.ok(user)} 比 {@code new Result<>().setCode(200)...} 清晰。</li>
 *   <li>内部 build 逻辑集中，将来要加字段（如 traceId）只改一处。</li>
 * </ol>
 *
 * @param <T> 业务数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    /** 业务状态码，200 表示成功，其余为各种失败（参见 {@link ResultCode}） */
    private Integer code;

    /** 人类可读的消息，成功时通常 "OK"，失败时为具体原因 */
    private String message;

    /** 业务数据载荷，失败时通常为 null */
    private T data;

    /**
     * 构造成功响应（无数据）。
     * <p>适用于 POST/PUT/DELETE 只需告诉前端"操作成功"的场景。</p>
     *
     * @param <T> 数据类型（此处不使用）
     * @return {@code {code:200, message:"OK"}}
     */
    public static <T> Result<T> ok() {
        return build(ResultCode.SUCCESS, null);
    }

    /**
     * 构造成功响应（带数据）。
     *
     * @param data 要返回给调用方的业务数据
     * @param <T>  数据类型
     * @return {@code {code:200, message:"OK", data:<data>}}
     */
    public static <T> Result<T> ok(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    /**
     * 用预定义的 {@link ResultCode} 构造失败响应。
     *
     * @param code 参考 {@link ResultCode} 中的枚举值
     * @param <T>  数据类型（失败时不会带数据）
     * @return {@code {code:<code>, message:<该枚举的默认 message>}}
     */
    public static <T> Result<T> fail(ResultCode code) {
        return build(code, null);
    }

    /**
     * 用自定义状态码 + 消息构造失败响应。
     * <p>主要给 {@link com.leeinx.smartsync.common.exception.GlobalExceptionHandler} 这种需要透传业务异常消息的地方使用。</p>
     *
     * @param code    状态码
     * @param message 错误消息（可能动态拼接，如校验失败的字段名）
     * @param <T>     数据类型
     * @return 组装好的失败响应
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /**
     * 私有构建方法，统一管理 Result 的创建流程。
     * <p>之所以设为 private，是为了强制外部只能通过 {@code ok()/fail()} 这类语义方法创建实例，避免误用。</p>
     *
     * @param rc   {@link ResultCode} 枚举，提供 code 和默认 message
     * @param data 业务数据（允许 null）
     */
    private static <T> Result<T> build(ResultCode rc, T data) {
        Result<T> r = new Result<>();
        r.setCode(rc.getCode());
        r.setMessage(rc.getMessage());
        r.setData(data);
        return r;
    }
}
