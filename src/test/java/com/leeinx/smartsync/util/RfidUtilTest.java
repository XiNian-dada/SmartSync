package com.leeinx.smartsync.util;

import com.leeinx.smartsync.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/** {@link RfidUtil} 的单元测试。 */
class RfidUtilTest {

    /** 被测对象。 */
    private RfidUtil rfidUtil;

    /** 手动注入测试用 HMAC 密钥，不启动 Spring 上下文。 */
    @BeforeEach
    void setUp() {
        rfidUtil = new RfidUtil();
        ReflectionTestUtils.setField(rfidUtil, "hmacKey", "unit-test-hmac-key-1234567890ABCDEF");
        rfidUtil.init();
    }

    /** 生成后的 RFID 应能被正确还原。 */
    @Test
    void generateAndVerify_roundTrip() {
        String uuid = "ABCDEFGHJKMN";
        String rfid = rfidUtil.generate(uuid);
        assertEquals(13, rfid.length());
        assertEquals(uuid, rfidUtil.verify(rfid));
    }

    /** 校验对大小写不敏感。 */
    @Test
    void verify_caseInsensitive() {
        String rfid = rfidUtil.generate("ABCDEFGHJKMN");
        assertEquals("ABCDEFGHJKMN", rfidUtil.verify(rfid.toLowerCase()));
    }

    /** 篡改校验位后应被识别出来。 */
    @Test
    void verify_tamperedChecksumRejected() {
        String rfid = rfidUtil.generate("ABCDEFGHJKMN");
        char wrongCheck = rfid.charAt(12) == '0' ? '1' : '0';
        String tampered = rfid.substring(0, 12) + wrongCheck;
        assertThrows(BusinessException.class, () -> rfidUtil.verify(tampered));
    }

    /** 长度错误或空值都应判定非法。 */
    @Test
    void verify_wrongLengthRejected() {
        assertThrows(BusinessException.class, () -> rfidUtil.verify("SHORT"));
        assertThrows(BusinessException.class, () -> rfidUtil.verify(null));
    }

    /** 非法字符应直接拒绝。 */
    @Test
    void verify_illegalCharRejected() {
        assertThrows(BusinessException.class, () -> rfidUtil.verify("ABCDEFGH!KMN0"));
    }
}
