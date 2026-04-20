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
 * 患者业务 Service 实现。
 *
 * <h2>{@link BeanUtils#copyProperties(Object, Object)}</h2>
 * Spring 的工具方法，按字段名复制 source → target 的 getter/setter。
 * 适合"DTO → Entity""Entity → VO"这类字段大量重合的场景。
 *
 * <h2>关于 RFID 大小写</h2>
 * 写入时统一大写（手环生产规范），查询时也转大写，避免前后端大小写不一致导致查不到。
 */
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final RfidUtil rfidUtil;

    /**
     * 新建患者。
     *
     * <h3>步骤</h3>
     * <ol>
     *   <li>DTO 字段拷贝到 Entity。</li>
     *   <li>status 默认为 1（在院）。</li>
     *   <li>若 rfidUuid 非空，检查唯一性。</li>
     *   <li>insert。</li>
     * </ol>
     */
    @Override
    @Transactional
    public Long create(PatientSaveDTO dto) {
        Patient p = new Patient();
        BeanUtils.copyProperties(dto, p);
        if (p.getStatus() == null) p.setStatus(1);
        checkRfidConflict(p.getRfidUuid(), null);
        patientMapper.insert(p);
        return p.getId();
    }

    /**
     * 更新患者。
     *
     * <h3>注意点</h3>
     * <ul>
     *   <li>先 {@link #getById(Long)} 确认患者存在——否则 updateById 静默失败（影响 0 行没有异常）。</li>
     *   <li>RFID 唯一性校验需排除当前患者自己（{@code excludeId = id}），否则自己改自己会冲突。</li>
     *   <li>{@code BeanUtils.copyProperties(dto, exist)} 会覆盖所有非 null 字段——包括 null 值！
     *       所以传 {@code rfidUuid=null} 可以实现"出院置空"的语义。</li>
     * </ul>
     */
    @Override
    @Transactional
    public void update(Long id, PatientSaveDTO dto) {
        Patient exist = getById(id);
        checkRfidConflict(dto.getRfidUuid(), id);
        BeanUtils.copyProperties(dto, exist);
        // copyProperties 可能覆盖掉 id（若 DTO 有同名字段会，但 PatientSaveDTO 没有 id 字段，实际不会改——兜底设一下更安全）
        exist.setId(id);
        patientMapper.updateById(exist);
    }

    /**
     * 逻辑删除。
     */
    @Override
    public void delete(Long id) {
        getById(id);
        patientMapper.deleteById(id);
    }

    /**
     * 按 id 查，失败抛 NOT_FOUND。
     */
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
     * <h3>LambdaQueryWrapper 中的 {@code .and(w -> ...)}</h3>
     * 生成 SQL 里一个加括号的子条件 {@code AND (name LIKE ? OR medical_record_no LIKE ? OR rfid_uuid = ?)}，
     * 避免 OR 污染外层的 {@code status} 等条件。
     */
    @Override
    public IPage<Patient> page(long current, long size, String keyword, Integer status) {
        Page<Patient> page = Page.of(current, size);
        LambdaQueryWrapper<Patient> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Patient::getName, keyword)
                    .or().like(Patient::getMedicalRecordNo, keyword)
                    .or().eq(Patient::getRfidUuid, keyword.toUpperCase()));
        }
        if (status != null) {
            q.eq(Patient::getStatus, status);
        }
        q.orderByDesc(Patient::getId);
        return patientMapper.selectPage(page, q);
    }

    /**
     * 终端拉取患者：双因子鉴权的<b>第二因子</b>（第一因子 JWT 已在 Filter 生效）。
     *
     * <h3>链路</h3>
     * <ol>
     *   <li>{@link RfidUtil#verify(String)} 校验 13 位格式 + HMAC 校验位。</li>
     *   <li>用 12 位 UUID 查"在院"患者（status=1）。</li>
     *   <li>Entity → VO，只返回必要字段。</li>
     * </ol>
     *
     * @param rfid13 终端上传的 13 位 RFID
     * @return 患者 VO
     */
    @Override
    public PatientVO fetchByRfid(String rfid13) {
        String uuid = rfidUtil.verify(rfid13);
        Patient p = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getRfidUuid, uuid)
                .eq(Patient::getStatus, 1));
        if (p == null) {
            throw new BusinessException(ResultCode.RFID_NOT_BOUND);
        }
        PatientVO vo = new PatientVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }

    /**
     * 校验 RFID 唯一性：是否有其他患者占用了同一 UUID。
     *
     * <h3>为什么仅靠数据库 UNIQUE 索引不够</h3>
     * DB 唯一约束会报 {@code SQLIntegrityConstraintViolationException}，但 Spring 映射成 {@code DuplicateKeyException}，
     * 错误信息不友好。这里主动查询 + 抛业务异常，让前端拿到 {@code CONFLICT} 业务码和中文提示。
     *
     * @param rfidUuid  待校验的 UUID（null 或空跳过）
     * @param excludeId 更新时排除自己的 id；新建时传 null
     */
    private void checkRfidConflict(String rfidUuid, Long excludeId) {
        if (!StringUtils.hasText(rfidUuid)) return;
        LambdaQueryWrapper<Patient> q = new LambdaQueryWrapper<Patient>()
                .eq(Patient::getRfidUuid, rfidUuid.toUpperCase());
        Patient hit = patientMapper.selectOne(q);
        if (hit != null && (excludeId == null || !hit.getId().equals(excludeId))) {
            throw new BusinessException(ResultCode.CONFLICT, "该 RFID 已绑定其他患者");
        }
    }
}
