package com.leeinx.smartsync.module.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.module.patient.dto.PatientVO;
import com.leeinx.smartsync.module.patient.entity.Patient;

/**
 * 患者档案业务 Service 接口。
 *
 * <h2>范围</h2>
 * 只负责"患者这个人"的档案 CRUD 和 RFID 手环绑定。
 * 挂号、住院、诊断等就诊任务数据由医院挂号系统提供，本服务不持有。
 */
public interface PatientService {

    /**
     * 新建患者档案。
     *
     * @param dto 档案数据（身份证号必填，其他可选）
     * @return 新患者主键
     * @throws com.leeinx.smartsync.common.exception.BusinessException
     *         身份证号已存在 / rfidUuid 被占用
     */
    Long create(PatientSaveDTO dto);

    /**
     * 更新患者档案（含换手环 / 回收手环）。
     *
     * @param id  患者主键
     * @param dto 更新参数
     * @throws com.leeinx.smartsync.common.exception.BusinessException
     *         患者不存在 / 身份证号冲突 / rfidUuid 冲突
     */
    void update(Long id, PatientSaveDTO dto);

    /**
     * 逻辑删除。
     */
    void delete(Long id);

    /**
     * 按主键查患者档案。
     *
     * @throws com.leeinx.smartsync.common.exception.BusinessException 不存在
     */
    Patient getById(Long id);

    /**
     * 分页查询患者。
     *
     * @param current 当前页
     * @param size    页大小
     * @param keyword 关键词：模糊匹配 name / 精确匹配 idCardNo / phone / rfidUuid（任一）
     * @return 分页对象
     */
    IPage<Patient> page(long current, long size, String keyword);

    /**
     * <b>核心业务</b>：终端按 13 位 RFID 拉取当前绑定的患者档案。
     *
     * @param rfid13 13 位 RFID 字符串
     * @return 患者 VO
     * @throws com.leeinx.smartsync.common.exception.BusinessException
     *         RFID 格式错 / 校验位错 / 未绑定任何患者
     */
    PatientVO fetchByRfid(String rfid13);
}
