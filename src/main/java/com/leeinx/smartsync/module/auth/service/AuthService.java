package com.leeinx.smartsync.module.auth.service;

import com.leeinx.smartsync.module.auth.dto.LoginVO;
import com.leeinx.smartsync.module.auth.dto.TerminalLoginDTO;
import com.leeinx.smartsync.module.auth.dto.TerminalRegisterDTO;

/** 终端注册与登录服务。 */
public interface AuthService {
    // 先声明有这些函数供终端调用
    /** 注册新终端，返回主键。 */
    Long register(TerminalRegisterDTO dto);

    /** 登录成功后返回 JWT 和基础终端信息。 */
    LoginVO login(TerminalLoginDTO dto);
}
