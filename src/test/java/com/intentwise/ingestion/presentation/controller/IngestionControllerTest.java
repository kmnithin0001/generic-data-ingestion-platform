package com.intentwise.ingestion.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentwise.ingestion.application.usecase.StartIngestionUseCase;
import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.presentation.controller.v1.IngestionController;
import com.intentwise.ingestion.presentation.dto.StartIngestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StartIngestionUseCase startIngestionUseCase;

    @Test
    void shouldAcceptNewIngestionRequestAndReturn202() throws Exception {
        StartIngestionRequest request = new StartIngestionRequest(
                "Test Ingest",
                "https://api.example.com/v1/data",
                HttpMethodType.GET,
                AuthenticationType.NONE,
                Collections.emptyMap(),
                PaginationType.NONE,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        IngestionJob pendingJob = IngestionJob.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .status(JobStatus.PENDING)
                .startTime(LocalDateTime.now())
                .build();

        when(startIngestionUseCase.execute(any(), any(), any())).thenReturn(pendingJob);

        mockMvc.perform(post("/api/v1/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldReturn200ForIdempotencyKeyMatch() throws Exception {
        StartIngestionRequest request = new StartIngestionRequest(
                "Test Ingest",
                "https://api.example.com/v1/data",
                HttpMethodType.GET,
                AuthenticationType.NONE,
                Collections.emptyMap(),
                PaginationType.NONE,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        IngestionJob activeJob = IngestionJob.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .status(JobStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .idempotencyKey("idem-key-123")
                .build();

        when(startIngestionUseCase.execute(any(), eq("idem-key-123"), any())).thenReturn(activeJob);

        mockMvc.perform(post("/api/v1/ingest")
                .header("Idempotency-Key", "idem-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.idempotencyKey").value("idem-key-123"));
    }

    @Test
    void shouldFailValidationForInvalidUrl() throws Exception {
        StartIngestionRequest request = new StartIngestionRequest(
                "Test Ingest",
                "not-a-valid-url",
                HttpMethodType.GET,
                AuthenticationType.NONE,
                Collections.emptyMap(),
                PaginationType.NONE,
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        mockMvc.perform(post("/api/v1/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
