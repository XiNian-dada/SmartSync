package com.leeinx.smartsync.module.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.leeinx.smartsync.common.api.PageResult;
import com.leeinx.smartsync.common.api.Result;
import com.leeinx.smartsync.module.patient.dto.FetchByRfidDTO;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.module.patient.dto.PatientVO;
import com.leeinx.smartsync.module.patient.entity.Patient;
import com.leeinx.smartsync.module.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者信息 Controller：管理端 CRUD + 终端核心业务接口（{@code /fetch-by-rfid}）。
 *
 * <h2>REST 风格约定</h2>
 * <ul>
 *   <li>POST /api/patient —— 创建</li>
 *   <li>PUT /api/patient/{id} —— 整体更新</li>
 *   <li>DELETE /api/patient/{id} —— 删除</li>
 *   <li>GET /api/patient/{id} —— 查详情</li>
 *   <li>GET /api/patient/page —— 列表查询（加 {@code /page} 表明是分页而非全量）</li>
 * </ul>
 */
@Tag(name = "03. 患者信息")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * 新建患者并绑定手环。
     *
     * @param dto 患者数据
     * @return 新患者 ID
     */
    @Operation(summary = "新建患者并绑定手环")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PatientSaveDTO dto) {
        return Result.ok(patientService.create(dto));
    }

    /**
     * 更新患者（PUT 语义：整体替换字段；null 值也会被覆盖进去）。
     *
     * @param id  患者 ID
     * @param dto 更新数据
     */
    @Operation(summary = "更新患者（支持换手环 / 出院置空 rfidUuid）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PatientSaveDTO dto) {
        patientService.update(id, dto);
        return Result.ok();
    }

    /**
     * 逻辑删除。
     */
    @Operation(summary = "删除患者")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.ok();
    }

    /**
     * 查详情。
     */
    @Operation(summary = "患者详情")
    @GetMapping("/{id}")
    public Result<Patient> get(@PathVariable Long id) {
        return Result.ok(patientService.getById(id));
    }

    /**
     * 分页查询。
     *
     * @param current 当前页
     * @param size    每页
     * @param keyword 关键词（name / 病历号 / rfidUuid 任一）
     * @param status  状态过滤
     */
    @Operation(summary = "分页查询患者")
    @GetMapping("/page")
    public Result<PageResult<Patient>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<Patient> p = patientService.page(current, size, keyword, status);
        return Result.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /**
     * <b>核心接口</b>：分布式终端扫手环后调用。
     *
     * <h3>调用前提</h3>
     * <ol>
     *   <li>Header 必须带 {@code Authorization: Bearer <JWT>}（{@link com.leeinx.smartsync.security.JwtAuthenticationFilter}）。</li>
     *   <li>Body 的 RFID 必须通过 HMAC 校验位验证（{@link com.leeinx.smartsync.util.RfidUtil#verify(String)}）。</li>
     * </ol>
     *
     * <h3>返回</h3>
     * 返回在院患者信息 VO。若患者已出院或手环未绑定任何人，返回 {@code RFID_NOT_BOUND} 业务错误。
     */
    @Operation(summary = "终端按 RFID 拉取当前在院患者（JWT + RFID 双因子校验）")
    @PostMapping("/fetch-by-rfid")
    public Result<PatientVO> fetchByRfid(@Valid @RequestBody FetchByRfidDTO dto) {
        return Result.ok(patientService.fetchByRfid(dto.getRfid()));
    }
}
