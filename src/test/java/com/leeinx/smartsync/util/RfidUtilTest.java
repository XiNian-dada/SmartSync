package com.leeinx.smartsync.util;

import com.leeinx.smartsync.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link RfidUtil} 的单元测试。
 *
 * <h2>为什么不是 {@code @SpringBootTest}</h2>
 * {@link RfidUtil} 唯一的外部依赖是 {@code @Value} 注入的配置项。
 * 单元测试里我们用 {@link ReflectionTestUtils#setField} 直接给字段赋值，不启动 Spring 上下文——
 * 这样测试启动<b>毫秒级</b>，而 {@code @SpringBootTest} 要几秒。
 *
 * <h2>测试覆盖点</h2>
 * <ol>
 *   <li>生成 → 验证：往返一致</li>
 *   <li>大小写不敏感</li>
 *   <li>校验位被篡改 → 抛异常</li>
 *   <li>长度不合法 → 抛异常</li>
 *   <li>非 Base32 字符 → 抛异常</li>
 * </ol>
 *
 * <h2>AAA 模式</h2>
 * 每个测试用例按 Arrange-Act-Assert 组织：准备数据 → 执行动作 → 断言结果。
 */
class RfidUtilTest {

    /** 被测对象，每个测试独立一份（避免测试间干扰） */
    private RfidUtil rfidUtil;

    /**
     * 每个测试方法执行前跑一次：手动构造 RfidUtil 并注入测试密钥。
     * <p>这里密钥必须 &ge; 16 字节，否则 {@link RfidUtil#init()} 会抛异常（故意的，见实现类注释）。</p>
     */
    @BeforeEach
    void setUp() {
        rfidUtil = new RfidUtil();
        ReflectionTestUtils.setField(rfidUtil, "hmacKey", "unit-test-hmac-key-1234567890ABCDEF");
        rfidUtil.init();
    }

    /**
     * 基础用例：生成的 RFID 能被自己 verify 通过，且 verify 返回原 UUID。
     */
    @Test
    void generateAndVerify_roundTrip() {
        String uuid = "ABCDEFGHJKMN";
        String rfid = rfidUtil.generate(uuid);
        assertEquals(13, rfid.length());
        assertEquals(uuid, rfidUtil.verify(rfid));
    }

    /**
     * verify 支持大小写混用（终端读卡器输出可能不一致）。
     */
    @Test
    void verify_caseInsensitive() {
        String rfid = rfidUtil.generate("ABCDEFGHJKMN");
        assertEquals("ABCDEFGHJKMN", rfidUtil.verify(rfid.toLowerCase()));
    }

    /**
     * 改掉校验位一个字符，应该被识别为篡改。
     */
    @Test
    void verify_tamperedChecksumRejected() {
        String rfid = rfidUtil.generate("ABCDEFGHJKMN");
        char wrongCheck = rfid.charAt(12) == '0' ? '1' : '0';
        String tampered = rfid.substring(0, 12) + wrongCheck;
        assertThrows(BusinessException.class, () -> rfidUtil.verify(tampered));
    }

    /**
     * 长度错误 / null 应抛异常。
     */
    @Test
    void verify_wrongLengthRejected() {
        assertThrows(BusinessException.class, () -> rfidUtil.verify("SHORT"));
        assertThrows(BusinessException.class, () -> rfidUtil.verify(null));
    }

    /**
     * 含非 Base32 字符应抛异常（Base32 表不含 '!'）。
     */
    @Test
    void verify_illegalCharRejected() {
        assertThrows(BusinessException.class, () -> rfidUtil.verify("ABCDEFGH!KMN0"));
    }
}
