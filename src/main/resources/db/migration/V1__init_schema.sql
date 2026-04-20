-- =====================================================
-- SmartSync 初始化表结构
-- V1: terminal（终端） + patient（患者）
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
    `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rfid_uuid`         VARCHAR(12) DEFAULT NULL COMMENT '当前手环 12 位 UUID（出院后置空）',
    `name`              VARCHAR(64) NOT NULL COMMENT '患者姓名',
    `gender`            TINYINT     DEFAULT NULL COMMENT '1男 2女',
    `age`               INT         DEFAULT NULL,
    `medical_record_no` VARCHAR(64) DEFAULT NULL COMMENT '病历号',
    `ward`              VARCHAR(64) DEFAULT NULL COMMENT '病区',
    `bed_no`            VARCHAR(32) DEFAULT NULL COMMENT '床号',
    `diagnosis`         TEXT COMMENT '诊断摘要',
    `admission_at`      DATETIME    DEFAULT NULL COMMENT '入院时间',
    `discharge_at`      DATETIME    DEFAULT NULL COMMENT '出院时间',
    `status`            TINYINT     NOT NULL DEFAULT 1 COMMENT '0出院 1在院',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`           TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rfid_active` (`rfid_uuid`, `deleted`),
    KEY `idx_medical_record_no` (`medical_record_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '患者信息';
