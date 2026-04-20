package com.leeinx.smartsync.module.auth.service;

import com.leeinx.smartsync.module.auth.dto.LoginVO;
import com.leeinx.smartsync.module.auth.dto.TerminalLoginDTO;
import com.leeinx.smartsync.module.auth.dto.TerminalRegisterDTO;

/**
 * 鉴权服务接口。
 *
 * <h2>为什么 Service 要拆成接口 + 实现类</h2>
 * <ol>
 *   <li><b>单元测试</b>：测试 Controller 时可以 mock {@code AuthService}，不启动真实的 DB。</li>
 *   <li><b>多实现扩展</b>：将来如果要支持"LDAP 登录"和"密码登录"并存，只要多写一个 {@code LdapAuthServiceImpl}，
 *       Controller 里注入接口即可切换。</li>
 *   <li><b>Spring AOP 代理</b>：基于 JDK 动态代理要求被代理对象实现接口（当前 Spring 默认用 CGLIB 可以代理普通类，
 *       但接口方式兼容性最好，仍是推荐做法）。</li>
 * </ol>
 */
public interface AuthService {

    /**
     * 终端自助注册。
     *
     * @param dto 终端提供的编码、密钥、名称
     * @return 新终端的数据库主键（可用于管理员后续审核接口）
     * @throws com.leeinx.smartsync.common.exception.BusinessException 当 terminalCode 已存在时
     */
    Long register(TerminalRegisterDTO dto);

    /**
     * 终端登录换取 JWT。
     *
     * @param dto 登录凭据
     * @return 包含 token、过期时间、终端编码的 VO
     * @throws com.leeinx.smartsync.common.exception.BusinessException 密码错误 / 终端未审核
     */
    LoginVO login(TerminalLoginDTO dto);
}
