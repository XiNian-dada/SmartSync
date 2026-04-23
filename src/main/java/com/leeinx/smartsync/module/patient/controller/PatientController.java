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

/** 患者档案接口，包含 CRUD 和按 RFID 拉取患者。 */
@Tag(name = "03. 患者档案")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    /** 新建患者档案。 */
    @Operation(summary = "新建患者档案")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PatientSaveDTO dto) {
        return Result.ok(patientService.create(dto));
    }

    /** 更新患者档案，也支持换绑或回收手环。 */
    @Operation(summary = "更新患者档案（含绑定/回收手环）")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PatientSaveDTO dto) {
        patientService.update(id, dto);
        return Result.ok();
    }

    /** 逻辑删除患者档案。 */
    @Operation(summary = "删除患者档案")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.ok();
    }

    /** 查询患者详情。 */
    @Operation(summary = "患者档案详情")
    @GetMapping("/{id}")
    public Result<Patient> get(@PathVariable Long id) {
        return Result.ok(patientService.getById(id));
    }

    /** 分页查询患者，支持姓名模糊和关键字段精确检索。 */
    @Operation(summary = "分页查询患者")
    @GetMapping("/page")
    public Result<PageResult<Patient>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        IPage<Patient> p = patientService.page(current, size, keyword);
        return Result.ok(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    /** 核心接口：终端凭 JWT 和 RFID 拉取患者档案。 */
    @Operation(summary = "终端按 RFID 拉取患者档案（JWT + RFID 双因子校验）")
    @PostMapping("/fetch-by-rfid")
    public Result<PatientVO> fetchByRfid(@Valid @RequestBody FetchByRfidDTO dto) {
        return Result.ok(patientService.fetchByRfid(dto.getRfid()));
    }
}
