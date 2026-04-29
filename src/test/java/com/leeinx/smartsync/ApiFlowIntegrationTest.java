package com.leeinx.smartsync;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.leeinx.smartsync.module.auth.dto.TerminalLoginDTO;
import com.leeinx.smartsync.module.auth.dto.TerminalRegisterDTO;
import com.leeinx.smartsync.module.patient.dto.FetchByRfidDTO;
import com.leeinx.smartsync.module.patient.dto.PatientSaveDTO;
import com.leeinx.smartsync.support.SqliteIntegrationTestSupport;
import com.leeinx.smartsync.util.RfidUtil;

class ApiFlowIntegrationTest extends SqliteIntegrationTestSupport {

    @Autowired
    private RfidUtil rfidUtil;

    @Test
    void protectedEndpointRequiresJwt() throws Exception {
        mockMvc.perform(get("/api/terminal/page"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未认证或凭证失效"));
    }

    @Test
    void duplicateTerminalCodeReturnsBusinessError() throws Exception {
        registerTerminal("T001", "终端一", "secret123");

        TerminalRegisterDTO duplicate = registerDto("T001", "终端二", "secret123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4102))
                .andExpect(jsonPath("$.message").value("终端编码已存在"));
    }

    @Test
    void loginBeforeActivationReturnsBusinessError() throws Exception {
        registerTerminal("T002", "待审核终端", "secret123");

        TerminalLoginDTO login = loginDto("T002", "secret123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4101))
                .andExpect(jsonPath("$.message").value("终端未启用或待审核"));
    }

    @Test
    void activatedTerminalCanLoginAndAccessProtectedEndpoint() throws Exception {
        long terminalId = registerTerminal("T003", "已启用终端", "secret123");
        activateTerminal(terminalId);

        String token = loginAndGetToken("T003", "secret123");

        mockMvc.perform(get("/api/terminal/page")
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].terminalCode").value("T003"));
    }

    @Test
    void patientCanBeCreatedAndFetchedByRfidThroughProtectedApi() throws Exception {
        String token = prepareActiveTerminalToken("T004");
        String uuid = "ABCDEFGHJKMN";

        PatientSaveDTO patient = patientDto(uuid, "110101199003078888", "张三");
        mockMvc.perform(post("/api/patient")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());

        FetchByRfidDTO fetch = new FetchByRfidDTO();
        fetch.setRfid(rfidUtil.generate(uuid));

        mockMvc.perform(post("/api/patient/fetch-by-rfid")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fetch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三"))
                .andExpect(jsonPath("$.data.idCardNo").value("110101199003078888"))
                .andExpect(jsonPath("$.data.rfidUuid").value(uuid));
    }

    @Test
    void duplicatePatientIdCardReturnsBusinessError() throws Exception {
        String token = prepareActiveTerminalToken("T005");

        mockMvc.perform(post("/api/patient")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                patientDto("ABCDEFGHJKMN", "110101199003078888", "张三"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/patient")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                patientDto("ABCDEFGHJKMP", "110101199003078888", "李四"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(4301))
                .andExpect(jsonPath("$.message").value("该身份证号已存在患者档案"));
    }

    private String prepareActiveTerminalToken(String code) throws Exception {
        long terminalId = registerTerminal(code, "测试终端-" + code, "secret123");
        activateTerminal(terminalId);
        return loginAndGetToken(code, "secret123");
    }

    private long registerTerminal(String code, String name, String secret) throws Exception {
        TerminalRegisterDTO register = registerDto(code, name, secret);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return readJson(result.getResponse().getContentAsString()).path("data").asLong();
    }

    private void activateTerminal(long terminalId) {
        jdbcTemplate.update("UPDATE terminal SET status = 1 WHERE id = ?", terminalId);
    }

    private String loginAndGetToken(String code, String secret) throws Exception {
        TerminalLoginDTO login = loginDto(code, secret);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode json = readJson(result.getResponse().getContentAsString());
        return json.path("data").path("token").asText();
    }

    private TerminalRegisterDTO registerDto(String code, String name, String secret) {
        TerminalRegisterDTO dto = new TerminalRegisterDTO();
        dto.setTerminalCode(code);
        dto.setTerminalName(name);
        dto.setSecretKey(secret);
        return dto;
    }

    private TerminalLoginDTO loginDto(String code, String secret) {
        TerminalLoginDTO dto = new TerminalLoginDTO();
        dto.setTerminalCode(code);
        dto.setSecretKey(secret);
        return dto;
    }

    private PatientSaveDTO patientDto(String rfidUuid, String idCardNo, String name) {
        PatientSaveDTO dto = new PatientSaveDTO();
        dto.setRfidUuid(rfidUuid);
        dto.setIdCardNo(idCardNo);
        dto.setInsuranceNo("YB" + idCardNo.substring(idCardNo.length() - 6));
        dto.setPhone("13800138000");
        dto.setName(name);
        dto.setGender(1);
        dto.setAge(30);
        dto.setMedicalHistory("无");
        dto.setEmergencyContactName("家属");
        dto.setEmergencyContactPhone("13900139000");
        return dto;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
