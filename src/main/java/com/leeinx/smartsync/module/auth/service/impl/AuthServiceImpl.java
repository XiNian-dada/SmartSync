package com.leeinx.smartsync.module.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;
import com.leeinx.smartsync.module.auth.dto.LoginVO;
import com.leeinx.smartsync.module.auth.dto.TerminalLoginDTO;
import com.leeinx.smartsync.module.auth.dto.TerminalRegisterDTO;
import com.leeinx.smartsync.module.auth.service.AuthService;
import com.leeinx.smartsync.module.terminal.entity.Terminal;
import com.leeinx.smartsync.module.terminal.mapper.TerminalMapper;
import com.leeinx.smartsync.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 鉴权服务实现。
 *
 * <h2>依赖注入</h2>
 * Lombok 的 {@code @RequiredArgsConstructor} 自动生成含所有 {@code final} 字段的构造器，Spring 用<b>构造器注入</b>填充：
 * <ul>
 *   <li>{@link TerminalMapper} —— MyBatis-Plus 自动生成的 Mapper 代理</li>
 *   <li>{@link PasswordEncoder} —— 从 {@link com.leeinx.smartsync.config.SecurityConfig} 注入的 BCrypt 实现</li>
 *   <li>{@link JwtUtil} —— JWT 工具</li>
 * </ul>
 *
 * <h2>为什么加 {@code @Service} 而不是 {@code @Component}</h2>
 * Spring 对这些 stereotype 注解的处理本质一样，但语义不同。按分层约定：
 * <ul>
 *   <li>{@code @Controller} / {@code @RestController} —— 接口层</li>
 *   <li>{@code @Service} —— 业务层</li>
 *   <li>{@code @Repository} —— 数据访问层（MyBatis-Plus 的 Mapper 本质被 Spring 当 Repository 处理）</li>
 *   <li>{@code @Component} —— 其他通用 Bean（配置类、工具类等）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TerminalMapper terminalMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** JWT 有效期（小时），用于计算 {@link LoginVO#getExpiresIn()} */
    @Value("${smartsync.jwt.expire-hours}")
    private long expireHours;

    /**
     * 注册终端。
     *
     * <h3>步骤</h3>
     * <ol>
     *   <li>用 Lambda 条件查询，看 terminalCode 是否已存在。</li>
     *   <li>不存在则 BCrypt 加密密钥，存 DB，默认 status=0（待审核）。</li>
     *   <li>返回新记录主键。</li>
     * </ol>
     *
     * <h3>{@code @Transactional} 的作用</h3>
     * 一旦 insert 后面的代码抛异常，整个方法的 DB 操作都会回滚。虽然本方法只有一条 insert，但加事务是好习惯——
     * 将来扩展（如"注册时顺便发审核通知"）也能保证原子性。
     *
     * @param dto 注册参数
     * @return 新终端 ID
     * @throws BusinessException 编码已存在
     */
    @Override
    @Transactional
    public Long register(TerminalRegisterDTO dto) {
        // Wrappers.lambdaQuery 是 MyBatis-Plus 的便捷写法，等价于 new LambdaQueryWrapper<Terminal>()
        // 好处：字段用方法引用（Terminal::getTerminalCode），编译期检查，重构改字段名不会漏
        Long existing = terminalMapper.selectCount(
                Wrappers.<Terminal>lambdaQuery().eq(Terminal::getTerminalCode, dto.getTerminalCode()));
        if (existing != null && existing > 0) {
            throw new BusinessException(ResultCode.TERMINAL_CODE_EXISTS);
        }
        Terminal t = new Terminal();
        t.setTerminalCode(dto.getTerminalCode());
        t.setTerminalName(dto.getTerminalName());
        // BCrypt 内置 salt，每次调用 encode 结果都不同，即使两终端密钥相同，DB 里哈希也不同
        t.setSecretHash(passwordEncoder.encode(dto.getSecretKey()));
        t.setStatus(0);
        terminalMapper.insert(t);
        // insert 后，MyBatis-Plus 会把数据库生成的主键回填到实体（通过 useGeneratedKeys）
        return t.getId();
    }

    /**
     * 终端登录。
     *
     * <h3>步骤</h3>
     * <ol>
     *   <li>按 terminalCode 查终端记录。</li>
     *   <li>用 {@link PasswordEncoder#matches(CharSequence, String)} 比对密钥明文和数据库哈希。</li>
     *   <li>检查 status，只有 {@code 1}（启用）才允许登录。</li>
     *   <li>生成 JWT，更新 lastLoginAt。</li>
     * </ol>
     *
     * <h3>安全细节</h3>
     * "密码错"和"用户不存在"都返回同一种错误（{@code LOGIN_FAILED}），不告诉调用方"这个用户不存在"——
     * 避免被用作"用户名枚举攻击"（攻击者扫描哪些账号真实存在）。
     *
     * @param dto 登录参数
     * @return 含 JWT 的 VO
     * @throws BusinessException 账号/密码错误 或 终端未审核
     */
    @Override
    public LoginVO login(TerminalLoginDTO dto) {
        Terminal t = terminalMapper.selectOne(
                Wrappers.<Terminal>lambdaQuery().eq(Terminal::getTerminalCode, dto.getTerminalCode()));
        if (t == null || !passwordEncoder.matches(dto.getSecretKey(), t.getSecretHash())) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        if (t.getStatus() == null || t.getStatus() != 1) {
            throw new BusinessException(ResultCode.TERMINAL_NOT_ACTIVE);
        }
        String token = jwtUtil.generate(t.getId(), t.getTerminalCode());
        // 更新最近登录时间，便于管理员查看终端活跃情况
        t.setLastLoginAt(LocalDateTime.now());
        terminalMapper.updateById(t);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(expireHours * 3600L);
        vo.setTerminalCode(t.getTerminalCode());
        return vo;
    }
}
