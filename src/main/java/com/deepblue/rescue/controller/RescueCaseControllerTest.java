package com.deepblue.rescue.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.deepblue.exception.BusinessRuleException;
import com.deepblue.exception.ResourceNotFoundException;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.service.RescueCaseService;

@WebMvcTest(RescueCaseController.class)
@Import(GlobalExceptionHandler.class)
class RescueCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RescueCaseService service;

    private RescueCaseResponse sample(String code, RescueStatus status) {
        return new RescueCaseResponse(1L, code, LocalDate.of(2026, 8, 20),
                "Bahia Concha", status, "DB-CAR", "AN-2026-001");
    }

    @Test
    void shouldReturnRescueCaseByCode() throws Exception {
        when(service.findByCode("RES-2026-001"))
                .thenReturn(sample("RES-2026-001", RescueStatus.IN_REHABILITATION));

        mockMvc.perform(get("/api/rescue-cases/{code}", "RES-2026-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseCode").value("RES-2026-001"))
                .andExpect(jsonPath("$.status").value("IN_REHABILITATION"));

        verify(service).findByCode("RES-2026-001");
    }

    @Test
    void shouldReturn404WhenCaseDoesNotExist() throws Exception {
        when(service.findByCode("RES-999"))
                .thenThrow(new ResourceNotFoundException("Rescue case not found: RES-999"));

        mockMvc.perform(get("/api/rescue-cases/{code}", "RES-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Rescue case not found: RES-999"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void shouldReturnCasesByStatus() throws Exception {
        when(service.findByStatus(RescueStatus.IN_REHABILITATION)).thenReturn(List.of(
                sample("RES-001", RescueStatus.IN_REHABILITATION),
                sample("RES-002", RescueStatus.IN_REHABILITATION)));

        mockMvc.perform(get("/api/rescue-cases").param("status", "IN_REHABILITATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("IN_REHABILITATION"));

        verify(service).findByStatus(RescueStatus.IN_REHABILITATION);
    }

    @Test
    void shouldReturn400WhenStatusParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/rescue-cases").param("status", "FLYING"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.details.status").exists());

        verify(service, never()).findByStatus(any());
    }

    @Test
    void shouldChangeStatus() throws Exception {
        when(service.changeStatus(eq("RES-001"), any(ChangeRescueStatusRequest.class)))
                .thenReturn(sample("RES-001", RescueStatus.READY_FOR_RELEASE));

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "READY_FOR_RELEASE" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY_FOR_RELEASE"));

        verify(service).changeStatus(eq("RES-001"), any(ChangeRescueStatusRequest.class));
    }

    @Test
    void shouldReturn400WhenPatchBodyIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.status").value("Status is required"));

        verify(service, never()).changeStatus(anyString(), any());
    }

    @Test
    void shouldReturn409WhenTransitionIsInvalid() throws Exception {
        when(service.changeStatus(eq("RES-001"), any()))
                .thenThrow(new BusinessRuleException("Invalid status transition"));

        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "RELEASED" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid status transition"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void shouldReturn400WhenEnumInJsonIsInvalid() throws Exception {
        mockMvc.perform(patch("/api/rescue-cases/{code}/status", "RES-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "FLYING" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or invalid JSON request"));

        verify(service, never()).changeStatus(anyString(), any());
    }

    @Test
    void shouldReturn500OnUnexpectedError() throws Exception {
        when(service.findByCode("RES-500")).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/rescue-cases/{code}", "RES-500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}