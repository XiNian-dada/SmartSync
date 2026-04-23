package com.leeinx.smartsync.common.api;

import lombok.Getter;

/**
 * 系统级与业务级状态码定义。
 * 4100 段用于终端鉴权，4200 段用于 RFID，4300 段用于患者档案。
 */
@Getter 
// 为每个枚举添加 getter 方法
public enum ResultCode {

    /** 通用成功。 */
    SUCCESS(200, "OK"),
    /** 请求参数错误。 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未认证或凭证失效。 */
    UNAUTHORIZED(401, "未认证或凭证失效"),
    /** 已认证但没有权限。 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在。 */
    NOT_FOUND(404, "资源不存在"),
    /** 资源冲突。 */
    CONFLICT(409, "资源冲突"),
    /** 未预期的服务端异常。 */
    SERVER_ERROR(500, "服务器内部错误"),

    /** 终端未启用或待审核。 */
    TERMINAL_NOT_ACTIVE(4101, "终端未启用或待审核"),
    /** 终端编码重复。 */
    TERMINAL_CODE_EXISTS(4102, "终端编码已存在"),
    /** 终端编码或密钥错误。 */
    LOGIN_FAILED(4103, "终端编码或密钥错误"),

    /** RFID 长度或字符集非法。 */
    RFID_INVALID_FORMAT(4201, "RFID 格式非法"),
    /** RFID 校验位不匹配。 */
    RFID_CHECKSUM_FAIL(4202, "RFID 校验位不匹配"),
    /** RFID 已通过验签，但未绑定患者。 */
    RFID_NOT_BOUND(4203, "RFID 未绑定任何患者"),

    /** 身份证号重复。 */
    PATIENT_ID_CARD_EXISTS(4301, "该身份证号已存在患者档案"),
    /** RFID 已绑定其他患者。 */
    PATIENT_RFID_CONFLICT(4302, "该 RFID 已绑定其他患者");

    /** 对应 Result.code。 */
    private final Integer code;

    /** 对应 Result.message。 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
