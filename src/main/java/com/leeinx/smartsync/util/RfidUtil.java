package com.leeinx.smartsync.util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;

import jakarta.annotation.PostConstruct;

/**
 * RFID 工具类。
 * 负责生成和校验 `12 位 UUID + 1 位校验位` 的 RFID 字符串。
 */


/**
 * 根据这个类, 我们能知道SpringBoot处理组件的方式
 * 首先通过启动时读取带Component注解的类, 接管他的生命周期
 * 接下来, 根据 Value 注解, 从配置文件中读取相应数据并赋值给被注解字段
 * 接下来, 根据PostConstruct注解, 自动执行初始化函数, 这样这个类就可以直接被使用
 * 比如:
 * 在传统方法中, 我们使用这个类需要这样:
 * RfidUtil rfidUtil = new Rfidutil();
 * 而在SpringBoot中, 我们只需要使用
 * &#064;Autowired
 * RfidUtil rfidUtil;
 * 接下来注入数据到这个类中, 然后就可以使用这个类了
 * 比如可以使用SpringBoot提供的@Autowired注解来注入这个类, 然后就可以使用这个类了
 **/

//组件声明, 让Springboot接管他的生命周期, 不需要使用new来新建实例
@Component
public class RfidUtil {

    /** Crockford 风格 Base32 字母表。 */
    private static final String BASE32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

    /** UUID 部分长度。 */
    private static final int UUID_LEN = 12;

    /** 完整 RFID 长度。 */
    private static final int RFID_LEN = 13;

    /** 原始 HMAC 密钥字符串。
        Value 注解用于读取配置文件中的相应字段 并赋值给被声明的字段*/
    @Value("${smartsync.rfid.hmac-key}")
    private String hmacKey;

    /** 预先构造好的 HMAC key。 */
    private SecretKeySpec keySpec;

    /** 启动时校验并初始化 HMAC 密钥。 */
    @PostConstruct
    //这个PostConstruct注解是用于告知 Spring 在启动时自动执行这个初始化方法
    public void init() {
        if (hmacKey == null || hmacKey.getBytes(StandardCharsets.UTF_8).length < 16) {
            throw new IllegalStateException("smartsync.rfid.hmac-key 长度不足 16 字节");
        }
        this.keySpec = new SecretKeySpec(hmacKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** 根据 12 位 UUID 生成 13 位 RFID。 */
    public String generate(String uuid12) {
        validateUuid(uuid12);
        char check = computeCheckChar(uuid12.toUpperCase());
        return uuid12.toUpperCase() + check;
    }

    /**
     * 校验 13 位 RFID。
     * 成功时直接返回前 12 位 UUID，便于业务层继续查库。
     */
    public String verify(String rfid13) {
        if (rfid13 == null || rfid13.length() != RFID_LEN) {
            throw new BusinessException(ResultCode.RFID_INVALID_FORMAT);
        }
        String upper = rfid13.toUpperCase();
        String uuid = upper.substring(0, UUID_LEN);
        char actual = upper.charAt(UUID_LEN);
        validateUuid(uuid);
        char expected = computeCheckChar(uuid);
        if (expected != actual) {
            throw new BusinessException(ResultCode.RFID_CHECKSUM_FAIL);
        }
        return uuid;
    }

    /** 校验 UUID 段的长度和字符合法性。 */
    private void validateUuid(String uuid12) {
        if (uuid12 == null || uuid12.length() != UUID_LEN) {
            throw new BusinessException(ResultCode.RFID_INVALID_FORMAT);
        }
        for (int i = 0; i < UUID_LEN; i++) {
            if (BASE32_ALPHABET.indexOf(Character.toUpperCase(uuid12.charAt(i))) < 0) {
                throw new BusinessException(ResultCode.RFID_INVALID_FORMAT);
            }
        }
    }

    /**
     * 计算 1 位校验字符。
     * 这里取 digest 首字节低 5 bit，正好映射到 32 个 Base32 字符。
     */
    private char computeCheckChar(String uuid12) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] digest = mac.doFinal(uuid12.getBytes(StandardCharsets.UTF_8));
            int idx = digest[0] & 0x1F;
            return BASE32_ALPHABET.charAt(idx);
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            // 这里是基础设施级异常，直接上抛为非法状态即可。
            throw new IllegalStateException("HMAC 初始化失败", e);
        }
    }
}
