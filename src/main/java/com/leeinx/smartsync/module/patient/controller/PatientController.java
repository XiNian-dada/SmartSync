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
 * 患者档案 Controller：管理端 CRUD + 终端核心业务接口（{@code /fetch-by-rfid}）。
 */
@Tag(name = "03. 患者档案")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /**
     * 新建患者档案。
     *
     * @param dto 患者数据
     * @return 新患者 ID
     */
    @Operation(summary = "新建患者档案")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PatientSaveDTO dto) {
        return Result.ok(patientService.create(dto));
    }

    /**
     * 更新患者档案（可换手环 / 回收手环 / 修改病史等）。
     */
    @Operation(summary = "更新患者档案（含绑定/回收手环）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PatientSaveDTO dto) {
        patientService.update(id, dto);
        return Result.ok();
    }

    /** 逻辑删除。 */
    @Operation(summary = "删除患者档案")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.ok();
    }

    /** 查详情。 */
    @Operation(summary = "患者档案详情")
    @GetMapping("/{id}")
    public Result<Patient> get(@PathVariable Long id) {
        return Result.ok(patientService.getById(id));
    }

    /**
     * 分页查询。
     *
     * @param keyword 模糊姓名 / 精确匹配身份证号 / 手机号 / RFID
     */
    @Operation(summary = "分页查询患者")
    @GetMapping("/page")
    public Result<PageResult<Patient>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        IPage<Patient> p = patientService.page(current, size, keyword);
        return Result.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /**
     * <b>核心接口</b>：终端扫手环后下发患者档案。
     *
     * <h3>前提</h3>
     * <ol>
     *   <li>Header 带 {@code Authorization: Bearer <JWT>}</li>
     *   <li>Body 的 RFID 通过 HMAC 校验位验证</li>
     * </ol>
     */
    @Operation(summary = "终端按 RFID 拉取患者档案（JWT + RFID 双因子校验）")
    @PostMapping("/fetch-by-rfid")
    public Result<PatientVO> fetchByRfid(@Valid @RequestBody FetchByRfidDTO dto) {
        return Result.ok(patientService.fetchByRfid(dto.getRfid()));
    }
}
