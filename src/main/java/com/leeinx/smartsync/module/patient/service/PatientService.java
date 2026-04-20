package com.leeinx.smartsync.module.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.module.patient.dto.PatientVO;
import com.leeinx.smartsync.module.patient.entity.Patient;

/**
 * 患者业务 Service 接口。
 *
 * <h2>接口中的方法为什么要带 Javadoc</h2>
 * 实现类的 Javadoc IDE 通常不会在"接口调用处"自动显示，而接口 Javadoc 会。
 * 调用方看到的是接口方法，写接口 Javadoc 能第一时间告诉调用方"这个方法会抛什么异常、返回什么含义"。
 */
public interface PatientService {

    /**
     * 新建患者。
     *
     * @param dto 新建参数（姓名必填、rfidUuid 可选）
     * @return 新记录主键
     * @throws com.leeinx.smartsync.common.exception.BusinessException rfidUuid 已被其他患者占用
     */
    Long create(PatientSaveDTO dto);

    /**
     * 更新患者（含换手环 / 出院置空 rfidUuid）。
     *
     * @param id  患者主键
     * @param dto 更新参数
     * @throws com.leeinx.smartsync.common.exception.BusinessException 患者不存在 / rfidUuid 冲突
     */
    void update(Long id, PatientSaveDTO dto);

    /**
     * 逻辑删除患者。
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 按 id 查患者。
     *
     * @param id 主键
     * @return Entity
     * @throws com.leeinx.smartsync.common.exception.BusinessException 不存在
     */
    Patient getById(Long id);

    /**
     * 分页查询。
     *
     * @param current 当前页
     * @param size    页大小
     * @param keyword 关键词（模糊匹配 name / medicalRecordNo / rfidUuid）
     * @param status  状态过滤（可 null）
     * @return 分页对象
     */
    IPage<Patient> page(long current, long size, String keyword, Integer status);

    /**
     * <b>核心业务方法</b>：终端按 13 位 RFID 拉取在院患者。
     * <p>会做两件事：1) 校验 RFID 校验位；2) 查在院患者表。</p>
     *
     * @param rfid13 终端上传的完整 RFID 字符串
     * @return 患者 VO
     * @throws com.leeinx.smartsync.common.exception.BusinessException RFID 格式错 / 校验位错 / 未绑定在院患者
     */
    PatientVO fetchByRfid(String rfid13);
}
