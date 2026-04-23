package com.leeinx.smartsync.module.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leeinx.smartsync.common.api.ResultCode;
import com.leeinx.smartsync.common.exception.BusinessException;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.module.patient.dto.PatientVO;
import com.leeinx.smartsync.module.patient.entity.Patient;
import com.leeinx.smartsync.module.patient.mapper.PatientMapper;
import com.leeinx.smartsync.module.patient.service.PatientService;
import com.leeinx.smartsync.util.RfidUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 患者档案服务实现。
 * 重点规则是身份证号唯一、RFID 唯一，以及按 RFID 查询患者。
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final RfidUtil rfidUtil;

    /** 新建档案时先做唯一性校验，再插入数据库。 */
    @Override
    @Transactional
    public Long create(PatientSaveDTO dto) {
        Patient p = new Patient();
        BeanUtils.copyProperties(dto, p);
        checkIdCardConflict(p.getIdCardNo(), null);
        checkRfidConflict(p.getRfidUuid(), null);
        patientMapper.insert(p);
        return p.getId();
    }

    /** 更新时需要排除自身后再做唯一性校验。 */
    @Override
    @Transactional
    public void update(Long id, PatientSaveDTO dto) {
        Patient exist = getById(id);
        checkIdCardConflict(dto.getIdCardNo(), id);
        checkRfidConflict(dto.getRfidUuid(), id);
        BeanUtils.copyProperties(dto, exist);
        exist.setId(id);
        patientMapper.updateById(exist);
    }

    /** 删除前先确认记录存在。 */
    @Override
    public void delete(Long id) {
        getById(id);
        patientMapper.deleteById(id);
    }

    /** 按主键查患者，不存在时抛业务异常。 */
    @Override
    public Patient getById(Long id) {
        Patient p = patientMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return p;
    }

    /**
     * 分页查询患者。
     * 姓名走模糊匹配，其它关键信息走精确匹配。
     */
    @Override
    public IPage<Patient> page(long current, long size, String keyword) {
        Page<Patient> page = Page.of(current, size);
        LambdaQueryWrapper<Patient> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 用 and 包裹，避免 or 影响外层其它条件。
            q.and(w -> w.like(Patient::getName, keyword)
                    .or().eq(Patient::getIdCardNo, keyword)
                    .or().eq(Patient::getPhone, keyword)
                    .or().eq(Patient::getRfidUuid, keyword.toUpperCase()));
        }
        q.orderByDesc(Patient::getId);
        return patientMapper.selectPage(page, q);
    }

    /** 核心链路：先验 RFID，再按 UUID 查患者并返回 VO。 */
    @Override
    public PatientVO fetchByRfid(String rfid13) {
        String uuid = rfidUtil.verify(rfid13);
        Patient p = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getRfidUuid, uuid));
        if (p == null) {
            throw new BusinessException(ResultCode.RFID_NOT_BOUND);
        }
        PatientVO vo = new PatientVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }

    /** 检查身份证号是否已被其它患者占用。 */
    private void checkIdCardConflict(String idCardNo, Long excludeId) {
        if (!StringUtils.hasText(idCardNo)) return;
        Patient hit = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getIdCardNo, idCardNo));
        if (hit != null && (excludeId == null || !hit.getId().equals(excludeId))) {
            throw new BusinessException(ResultCode.PATIENT_ID_CARD_EXISTS);
        }
    }

    /** 检查 RFID 是否已绑定给其它患者。 */
    private void checkRfidConflict(String rfidUuid, Long excludeId) {
        if (!StringUtils.hasText(rfidUuid)) return;
        Patient hit = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getRfidUuid, rfidUuid.toUpperCase()));
        if (hit != null && (excludeId == null || !hit.getId().equals(excludeId))) {
            throw new BusinessException(ResultCode.PATIENT_RFID_CONFLICT);
        }
    }
}
