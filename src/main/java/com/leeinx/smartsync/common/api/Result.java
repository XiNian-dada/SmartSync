package com.leeinx.smartsync.common.api;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 统一接口返回结构：{@code code + message + data}。
 *
 * @param <T> 业务数据类型
 */
@Data //万能字段, 可以给所有字段添加getter setter 方法 toString equals hashCode;
@JsonInclude(JsonInclude.Include.NON_NULL) //即如果返回的某个字段是null, 那就不在json字段中展示这个字段
public class Result<T> implements Serializable {

    /** 业务状态码，约定见 {@link ResultCode}。 */
    private Integer code;

    /** 成功或失败的简短说明。 */
    private String message;

    /** 业务数据，失败时通常为空。 */
    private T data; //泛型,让代码更灵活

    /** 构造成功响应，无数据体。 */
    public static <T> Result<T> ok() { //声明一个 OK 方法, 但返回null
        return build(ResultCode.SUCCESS, null);
    }

    /** 构造成功响应，并返回业务数据。 */
    public static <T> Result<T> ok(T data) { // 返回data的方法
        return build(ResultCode.SUCCESS, data);
    }

    /** 用预定义状态码构造失败响应。 */
    public static <T> Result<T> fail(ResultCode code) { // 如果失败，则返回code和message
        return build(code, null);
    }

    /** 用自定义状态码和消息构造失败响应。 */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /** 内部统一的构造入口，避免在各处手写 setCode/setMessage。 */
    private static <T> Result<T> build(ResultCode rc, T data) { //传入结果码和数据，返回结果
        Result<T> r = new Result<>(); // 创建结果对象
        r.setCode(rc.getCode()); // 设置状态码
        r.setMessage(rc.getMessage()); //
        r.setData(data);
        return r;
    }
}
