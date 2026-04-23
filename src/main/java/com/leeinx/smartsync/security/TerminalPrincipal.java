package com.leeinx.smartsync.security;

import java.io.Serializable;

/**
 * 当前请求对应的终端身份。
 *
 * @param terminalId   终端主键。
 * @param terminalCode 终端编码。
 */

//记录类, JAVA16 新特性, 自动生成访问函数, 方法名是变量名
public record TerminalPrincipal(
        Long terminalId,
        String terminalCode) implements Serializable {
}
