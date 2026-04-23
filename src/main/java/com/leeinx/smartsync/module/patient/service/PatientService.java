package com.leeinx.smartsync.module.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.module.patient.dto.PatientVO;
import com.leeinx.smartsync.module.patient.entity.Patient;

/** 患者档案与 RFID 绑定服务。 */
public interface PatientService {
    //No news is good news
    //错误全部以throw抛出
    /** 新建患者档案。 */
    Long create(PatientSaveDTO dto);

    /** 更新患者档案，也包含换绑或回收手环。 */
    void update(Long id, PatientSaveDTO dto);

    /** 逻辑删除患者档案。 */
    void delete(Long id);

    /** 按主键查询患者档案。 */
    Patient getById(Long id);

    /** 分页查询患者档案。 */
    IPage<Patient> page(long current, long size, String keyword);

    /** 终端按 13 位 RFID 拉取当前绑定患者。 */
    PatientVO fetchByRfid(String rfid13);
}
