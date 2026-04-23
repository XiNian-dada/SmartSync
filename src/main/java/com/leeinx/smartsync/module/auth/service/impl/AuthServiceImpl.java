package com.leeinx.smartsync.module.auth.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * 鉴权服务实现。
 * 负责终端注册、密钥校验和 JWT 签发。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TerminalMapper terminalMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** JWT 有效期，返回给客户端时会换算成秒。 */
    @Value("${smartsync.jwt.expire-hours}")
    private long expireHours;

    /** 注册新终端，默认状态为待审核。 */
    @Override // 重写AuthService里的方法
    @Transactional
    public Long register(TerminalRegisterDTO dto) {
        //**
        // 虽然说, 返回值不大可能超过 int
        // 但为了程序安全 防止溢出等各种意外情况
        // Mybatis和 JDBC的规范都是比较建议用Long来规避风险
        // 而且呢, 因为泛型 T 只支持对象类型, 所以不能用long
        // Long和Integer是差不多的
        // 支持自动拆箱 装箱
        // */
        Long existing = terminalMapper 
        .selectCount(
                Wrappers
                .<Terminal>lambdaQuery() //构造一个查询构造器
                .eq(Terminal::getTerminalCode, dto.getTerminalCode()) //查询条件
            );
                //
        if (existing != null && existing > 0) { //
            throw new BusinessException(ResultCode.TERMINAL_CODE_EXISTS);
        } // 抛出一个已存在的错误

        //如果说不存在 那就创建这个终端
        Terminal t = new Terminal();
        t.setTerminalCode(dto.getTerminalCode());
        t.setTerminalName(dto.getTerminalName());
        // 数据库存的是哈希，不保存终端明文密钥。
        t.setSecretHash(passwordEncoder.encode(dto.getSecretKey()));
        t.setStatus(0);
        terminalMapper.insert(t);
        return t.getId(); //返回 ID
    }

    /**
     * 终端登录。
     * 不区分“账号不存在”和“密钥错误”，避免暴露可枚举的账号信息。
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
        t.setLastLoginAt(LocalDateTime.now());
        terminalMapper.updateById(t);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(expireHours * 3600L);
        vo.setTerminalCode(t.getTerminalCode());
        return vo;
    }
}
