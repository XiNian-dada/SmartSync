package com.leeinx.smartsync.util;

import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * RFID 校验工具：生成与验证"12 位 UUID + 1 位校验位"的手环标识。
 *
 * <h2>业务场景</h2>
 * 医院分布式终端读取患者手环（RFID 卡），上传到服务器换取患者信息。
 * 手环上写的是 13 位 Base32 字符：前 12 位是随机 UUID，第 13 位是服务器算出的校验位。
 * 任何人只要拿到未加密的 12 位 UUID，不知道服务器密钥就无法伪造最后一位校验位，从而防止"批量伪造手环读取数据"。
 *
 * <h2>为什么选 HMAC-SHA256</h2>
 * <ul>
 *   <li>比简单 CRC 强：CRC 只防"传输错误"，HMAC 防"故意伪造"。</li>
 *   <li>比 AES 加密轻量：这里不需要机密性，只需要完整性——UUID 本身不敏感，只要防篡改。</li>
 *   <li>标准化：Java {@link Mac} API 自带，不依赖三方库。</li>
 * </ul>
 *
 * <h2>Base32 字母表</h2>
 * RFC 4648 的 Base32 用 {@code A-Z2-7}，但人眼容易混淆 0/O、1/I。这里采用<b>Douglas Crockford 风格</b>的
 * 变体 {@code 0-9A-V}（32 个字符），后续如果要拓展"手工录入容错"，可以在这个字母集上做 O→0、I→1 的纠正。
 *
 * <h2>为什么是 Spring 组件</h2>
 * {@code @Component} 让它成为单例 Bean，其他 Service 可通过构造器注入使用。
 * HMAC 的 key 只需在启动时初始化一次（{@link #init()}），重复使用同一个 key 避免每次请求都做字符串→byte[] 的开销。
 */
@Component
public class RfidUtil {

    /**
     * Base32 字符表（Crockford 风格）。
     * <p>索引 0 对应 '0'，索引 31 对应 'V'。{@link #computeCheckChar(String)} 里用到。</p>
     */
    private static final String BASE32_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

    /** UUID 部分的长度 */
    private static final int UUID_LEN = 12;

    /** 完整 RFID 字符串长度（UUID + 1 位校验位） */
    private static final int RFID_LEN = 13;

    /** HMAC 密钥原文，来自 {@code smartsync.rfid.hmac-key} 配置。 */
    @Value("${smartsync.rfid.hmac-key}")
    private String hmacKey;

    /** 缓存的 HMAC key，{@link #init()} 中生成。 */
    private SecretKeySpec keySpec;

    /**
     * 启动时初始化 HMAC 密钥。
     * <p>检查密钥长度 &ge; 16 字节，太短的密钥会降低 HMAC 的安全强度。
     * 密钥问题在启动阶段就暴露，避免跑到生产才出错。</p>
     *
     * @throws IllegalStateException 密钥长度不足时抛出
     */
    @PostConstruct
    public void init() {
        if (hmacKey == null || hmacKey.getBytes(StandardCharsets.UTF_8).length < 16) {
            throw new IllegalStateException("smartsync.rfid.hmac-key 长度不足 16 字节");
        }
        this.keySpec = new SecretKeySpec(hmacKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * 根据 12 位 UUID 生成带校验位的 13 位 RFID。
     * <p>测试 / 管理端预生成手环时用。例如 {@code generate("ABCDEFGHJKMN")} 可能返回 {@code "ABCDEFGHJKMNX"}。</p>
     *
     * @param uuid12 12 位 Base32 字符的 UUID
     * @return 13 位大写 RFID 字符串
     * @throws BusinessException UUID 格式非法（长度错/含非 Base32 字符）
     */
    public String generate(String uuid12) {
        validateUuid(uuid12);
        char check = computeCheckChar(uuid12.toUpperCase());
        return uuid12.toUpperCase() + check;
    }

    /**
     * 校验终端上传的 13 位 RFID 字符串。
     *
     * <h3>校验步骤</h3>
     * <ol>
     *   <li>长度必须是 13。</li>
     *   <li>前 12 位每个字符都必须在 Base32 字母表内。</li>
     *   <li>重新计算服务器端校验位，与 RFID 的第 13 位比对（大小写不敏感）。</li>
     * </ol>
     *
     * <h3>为什么返回 12 位 UUID 而不是 boolean</h3>
     * 校验通过的同时，调用方几乎必然要拿 UUID 去查数据库。这里一次性返回，省去调用方再 substring 的步骤，也避免两处处理大小写不一致。
     *
     * @param rfid13 终端读到的 13 位 RFID 字符串（允许大小写混用）
     * @return 去除校验位、已转大写的 12 位 UUID
     * @throws BusinessException 格式非法或校验位不匹配
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

    /**
     * 验证 UUID 段的长度和字符合法性。
     * <p>私有方法，仅在本类内部被 {@link #verify} 和 {@link #generate} 复用。</p>
     *
     * @param uuid12 待验证的 12 位字符串
     * @throws BusinessException 长度 / 字符不合法
     */
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
     * 计算校验位：对 UUID 做 HMAC-SHA256，取结果首字节的低 5 bit 查 Base32 字母表。
     *
     * <h3>为什么取 5 bit（0~31）</h3>
     * Base32 字母表正好 32 个字符（2<sup>5</sup>），5 bit 刚好映射到 1 个字符。
     * 注意：5 bit 只能提供 32 种可能，被随机撞对的概率是 1/32 ≈ 3%。
     * 如果未来需要更强防御，可以改成取 2 位校验（25 bit 空间）。
     *
     * @param uuid12 已验证、已转大写的 12 位 UUID
     * @return 1 位校验字符
     */
    private char computeCheckChar(String uuid12) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] digest = mac.doFinal(uuid12.getBytes(StandardCharsets.UTF_8));
            // digest[0] 的低 5 bit 就是索引（0~31）
            int idx = digest[0] & 0x1F;
            return BASE32_ALPHABET.charAt(idx);
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            // HmacSHA256 是 JDK 内置算法，实际不可能抛 NoSuchAlgorithmException
            // 这里直接抛 IllegalStateException 让启动失败，不把 checked exception 甩给调用方
            throw new IllegalStateException("HMAC 初始化失败", e);
        }
    }
}
