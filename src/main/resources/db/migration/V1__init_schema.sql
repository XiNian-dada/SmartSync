-- =====================================================
-- SmartSync 初始化表结构
-- V1: terminal（分布式终端） + patient（患者个人档案）
--
-- 设计要点：
--   1. patient 表存"患者这个人"的持久档案（身份证/医保/联系方式/病史）；
--      挂号、住院、诊断等"本次就诊"的任务性数据由医院挂号系统负责，
--      SmartSync 不重复持有。
--   2. RFID 手环作为"访问令牌"绑定到患者：rfid_uuid 指向当前持有
--      手环的患者；手环回收后置空。
-- =====================================================

CREATE TABLE `terminal`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `terminal_code`  VARCHAR(64)  NOT NULL COMMENT '终端编码（登录账号）',
    `terminal_name`  VARCHAR(128) DEFAULT NULL COMMENT '终端显示名称',
    `secret_hash`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密后的密钥',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审核 1启用 2禁用',
    `last_login_at`  DATETIME     DEFAULT NULL COMMENT '最近登录时间',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_terminal_code` (`terminal_code`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分布式终端';

CREATE TABLE `patient`
(
    `id`                       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rfid_uuid`                VARCHAR(12) DEFAULT NULL COMMENT '当前绑定手环 12 位 UUID，未绑定时为空',
    -- 实名信息（生产环境建议加密存储，当前学习项目明文）
    `id_card_no`               VARCHAR(32) NOT NULL COMMENT '身份证号（唯一身份标识）',
    `insurance_no`             VARCHAR(64) DEFAULT NULL COMMENT '医保卡号',
    `phone`                    VARCHAR(20) DEFAULT NULL COMMENT '本人手机号',
    -- 基本信息
    `name`                     VARCHAR(64) NOT NULL COMMENT '姓名',
    `gender`                   TINYINT     DEFAULT NULL COMMENT '1男 2女',
    `age`                      INT         DEFAULT NULL COMMENT '年龄（TODO：生产建议用 birth_date 动态算）',
    -- 病史：患者长期性的健康背景（慢病、过敏、手术史等），非本次诊断
    `medical_history`          TEXT COMMENT '病史',
    -- 紧急联系人
    `emergency_contact_name`   VARCHAR(64) DEFAULT NULL COMMENT '紧急联系人姓名',
    `emergency_contact_phone`  VARCHAR(20) DEFAULT NULL COMMENT '紧急联系人电话',
    -- 审计字段
    `created_at`               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`               DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`                  TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    -- 同一个人同一时刻一个 RFID；rfid_uuid NULL 不占唯一性
    UNIQUE KEY `uk_rfid_active` (`rfid_uuid`, `deleted`),
    -- 身份证号全局唯一：同一个人在院只能有一条档案
    UNIQUE KEY `uk_id_card` (`id_card_no`, `deleted`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '患者个人档案';
