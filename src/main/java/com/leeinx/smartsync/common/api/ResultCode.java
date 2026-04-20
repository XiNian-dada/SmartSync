package com.leeinx.smartsync.common.api;

import lombok.Getter;

/**
 * 业务状态码枚举（错误字典）。
 *
 * <h2>为什么使用枚举而不是常量 int</h2>
 * <ol>
 *   <li><b>类型安全</b>：方法签名可以要求 {@code ResultCode} 而不是 {@code int}，调用方不可能传入一个根本没定义的状态码。</li>
 *   <li><b>集中管理</b>：所有可能的业务错误都在本文件一览无余，团队新成员看一眼就知道系统的错误边界。</li>
 *   <li><b>code 和 message 一一对应</b>：避免同一个错误码在不同地方写出不同文案。</li>
 * </ol>
 *
 * <h2>状态码规划</h2>
 * <ul>
 *   <li><b>200/4xx/500</b>：与 HTTP 标准状态码含义对齐，处理通用场景。</li>
 *   <li><b>4100 段</b>：终端 / 鉴权相关业务错误。</li>
 *   <li><b>4200 段</b>：RFID 相关业务错误。</li>
 *   <li>后续可继续按业务模块划分区段（例如 4300 段预留给药品管理等新模块）。</li>
 * </ul>
 *
 * <h2>Lombok 注解 {@code @Getter}</h2>
 * 自动为所有字段生成 getter，本类不需要 setter（枚举值不可变），所以没用 {@code @Data}。
 */
@Getter
public enum ResultCode {

    /** 通用：成功 */
    SUCCESS(200, "OK"),
    /** 通用：请求参数错误（如缺字段、格式非法） */
    BAD_REQUEST(400, "请求参数错误"),
    /** 通用：未登录或 token 失效 */
    UNAUTHORIZED(401, "未认证或凭证失效"),
    /** 通用：已登录但没权限访问该资源 */
    FORBIDDEN(403, "无权限访问"),
    /** 通用：查询的数据不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 通用：资源冲突（唯一约束、状态不匹配等） */
    CONFLICT(409, "资源冲突"),
    /** 通用：服务器内部异常，通常是未预期的 bug */
    SERVER_ERROR(500, "服务器内部错误"),

    /** 终端：终端还没被管理员审核，或被禁用 */
    TERMINAL_NOT_ACTIVE(4101, "终端未启用或待审核"),
    /** 终端：注册时编码已被占用 */
    TERMINAL_CODE_EXISTS(4102, "终端编码已存在"),
    /** 终端：登录账号或密码错误 */
    LOGIN_FAILED(4103, "终端编码或密钥错误"),

    /** RFID：不是 13 位 / 包含非法字符 */
    RFID_INVALID_FORMAT(4201, "RFID 格式非法"),
    /** RFID：最后一位校验位与服务器算出的不一致，可能被篡改 */
    RFID_CHECKSUM_FAIL(4202, "RFID 校验位不匹配"),
    /** RFID：校验通过但数据库里没有对应的在院患者 */
    RFID_NOT_BOUND(4203, "RFID 未绑定在院患者");

    /** 业务状态码（对应 Result.code 字段） */
    private final Integer code;

    /** 对应的默认错误消息（对应 Result.message 字段） */
    private final String message;

    /**
     * 枚举构造器（Java 枚举的构造器默认就是 private，不能从外部 new）。
     *
     * @param code    业务状态码
     * @param message 默认提示文案
     */
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
