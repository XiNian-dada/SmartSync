package com.leeinx.smartsync.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 当前请求的终端身份（Principal）。
 *
 * <h2>"Principal" 是什么</h2>
 * Spring Security 用 {@link org.springframework.security.core.Authentication Authentication} 对象表示"当前是谁"，
 * 里面有个 {@code principal} 字段——可以是用户名字符串，也可以是任意类型的"身份对象"。
 * <p>本项目的 principal 就是这个类：包含终端 ID 和终端编码，Controller 里可以通过 {@code @AuthenticationPrincipal} 直接注入：</p>
 * <pre>{@code
 *   @GetMapping("/me")
 *   public Result<?> me(@AuthenticationPrincipal TerminalPrincipal principal) {
 *       return Result.ok(principal.getTerminalCode());
 *   }
 * }</pre>
 *
 * <h2>Lombok 注解</h2>
 * <ul>
 *   <li>{@code @Getter}：生成 getter</li>
 *   <li>{@code @AllArgsConstructor}：生成含所有字段的构造器（{@code new TerminalPrincipal(1L, "T001")}）</li>
 * </ul>
 *
 * <h2>{@link Serializable}</h2>
 * Spring Security 的 {@code Authentication} 可能被序列化（如开启分布式 Session），身份对象也要能序列化。
 */
@Getter
@AllArgsConstructor
public class TerminalPrincipal implements Serializable {
    /** 终端数据库主键 */
    private final Long terminalId;
    /** 终端编码（登录账号） */
    private final String terminalCode;
}
