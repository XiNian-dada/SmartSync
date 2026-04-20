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
 * 患者档案 Service 实现。
 *
 * <h2>唯一性校验</h2>
 * 两个字段有业务唯一性：
 * <ul>
 *   <li>{@code idCardNo} —— 一个人只能有一份档案</li>
 *   <li>{@code rfidUuid} —— 一个手环只能绑一个患者</li>
 * </ul>
 * 虽然 DB 层有唯一索引兜底，但业务层主动查询 + 抛业务异常能给前端友好提示（而非 SQL 异常）。
 *
 * <h2>fetchByRfid 的变更</h2>
 * 早期版本用 {@code status=1（在院）} 过滤。现在 patient 表不再持有"在院"状态——
 * RFID 是否绑定即代表手环是否在用，直接查 {@code rfid_uuid=X} 就够了。
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final RfidUtil rfidUtil;

    /**
     * 新建患者档案。
     *
     * <h3>步骤</h3>
     * <ol>
     *   <li>DTO → Entity 拷贝</li>
     *   <li>校验 idCardNo 唯一</li>
     *   <li>校验 rfidUuid 唯一（若传了）</li>
     *   <li>insert</li>
     * </ol>
     */
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

    /**
     * 更新患者档案。
     * <p>两个唯一字段都要"排除自己"做冲突检查。</p>
     */
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

    /** 逻辑删除前先校验存在性。 */
    @Override
    public void delete(Long id) {
        getById(id);
        patientMapper.deleteById(id);
    }

    /** 按主键查，失败抛 NOT_FOUND。 */
    @Override
    public Patient getById(Long id) {
        Patient p = patientMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return p;
    }

    /**
     * 分页查询。
     *
     * <h3>keyword 多字段匹配规则</h3>
     * <ul>
     *   <li>{@code name} 用 LIKE 模糊匹配（患者姓名可能只记一部分）</li>
     *   <li>{@code idCardNo / phone / rfidUuid} 用 = 精确匹配（身份证号片段意义不大）</li>
     * </ul>
     * 用 {@code .and(w -> ...)} 包裹避免 OR 污染外层条件。
     */
    @Override
    public IPage<Patient> page(long current, long size, String keyword) {
        Page<Patient> page = Page.of(current, size);
        LambdaQueryWrapper<Patient> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Patient::getName, keyword)
                    .or().eq(Patient::getIdCardNo, keyword)
                    .or().eq(Patient::getPhone, keyword)
                    .or().eq(Patient::getRfidUuid, keyword.toUpperCase()));
        }
        q.orderByDesc(Patient::getId);
        return patientMapper.selectPage(page, q);
    }

    /**
     * 终端拉取患者：双因子的第二因子（JWT 已在 Filter 验证）。
     *
     * <h3>链路</h3>
     * <ol>
     *   <li>{@link RfidUtil#verify(String)} 校验 HMAC 校验位，返回 12 位 UUID</li>
     *   <li>按 UUID 查患者（无 status 过滤）</li>
     *   <li>命中则 Entity → VO 下发</li>
     * </ol>
     */
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

    /**
     * 检查身份证号唯一性。更新时排除自己；新建时 {@code excludeId=null}。
     *
     * @throws BusinessException 身份证号已被其他患者占用
     */
    private void checkIdCardConflict(String idCardNo, Long excludeId) {
        if (!StringUtils.hasText(idCardNo)) return;
        Patient hit = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getIdCardNo, idCardNo));
        if (hit != null && (excludeId == null || !hit.getId().equals(excludeId))) {
            throw new BusinessException(ResultCode.PATIENT_ID_CARD_EXISTS);
        }
    }

    /**
     * 检查 RFID 手环唯一性。
     *
     * @throws BusinessException 手环已被其他患者绑定
     */
    private void checkRfidConflict(String rfidUuid, Long excludeId) {
        if (!StringUtils.hasText(rfidUuid)) return;
        Patient hit = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getRfidUuid, rfidUuid.toUpperCase()));
        if (hit != null && (excludeId == null || !hit.getId().equals(excludeId))) {
            throw new BusinessException(ResultCode.PATIENT_RFID_CONFLICT);
        }
    }
}
