package com.intentwise.ingestion.presentation.controller;

import com.intentwise.ingestion.application.usecase.CancelJobUseCase;
import com.intentwise.ingestion.application.usecase.GetJobStatusUseCase;
import com.intentwise.ingestion.application.usecase.GetRawResponsesUseCase;
import com.intentwise.ingestion.application.usecase.ListJobsUseCase;
import com.intentwise.ingestion.application.usecase.RetryIngestionUseCase;
import com.intentwise.ingestion.domain.exception.DomainException;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.PageResult;
import com.intentwise.ingestion.presentation.controller.v1.JobController;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetryIngestionUseCase retryIngestionUseCase;

    @MockBean
    private CancelJobUseCase cancelJobUseCase;

    @MockBean
    private GetJobStatusUseCase getJobStatusUseCase;

    @MockBean
    private ListJobsUseCase listJobsUseCase;

    @MockBean
    private GetRawResponsesUseCase getRawResponsesUseCase;

    @Test
    void shouldGetJobDetailsSuccessfully() throws Exception {
        UUID jobId = UUID.randomUUID();
        IngestionJob job = IngestionJob.builder()
                .id(jobId)
                .sourceId(UUID.randomUUID())
                .status(JobStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .totalPagesFetched(2)
                .totalRecordsFetched(20)
                .percentageCompleted(50.0)
                .build();

        when(getJobStatusUseCase.execute(jobId)).thenReturn(job);

        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.percentageCompleted").value(50.0));
    }

    @Test
    void shouldReturn404WhenJobNotFound() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(getJobStatusUseCase.execute(jobId)).thenThrow(new DomainException("Job not found: " + jobId));

        mockMvc.perform(get("/api/v1/jobs/{jobId}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldRetryFailedJobSuccessfully() throws Exception {
        UUID jobId = UUID.randomUUID();
        IngestionJob retryJob = IngestionJob.builder()
                .id(UUID.randomUUID())
                .sourceId(UUID.randomUUID())
                .status(JobStatus.PENDING)
                .startTime(LocalDateTime.now())
                .build();

        when(retryIngestionUseCase.execute(eq(jobId), any())).thenReturn(retryJob);

        mockMvc.perform(post("/api/v1/jobs/{jobId}/retry", jobId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldCancelActiveJobSuccessfully() throws Exception {
        UUID jobId = UUID.randomUUID();
        IngestionJob cancelledJob = IngestionJob.builder()
                .id(jobId)
                .sourceId(UUID.randomUUID())
                .status(JobStatus.CANCELLED)
                .startTime(LocalDateTime.now())
                .build();

        when(cancelJobUseCase.execute(eq(jobId), any())).thenReturn(cancelledJob);

        mockMvc.perform(post("/api/v1/jobs/{jobId}/cancel", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void shouldListJobsWithPagination() throws Exception {
        PageResult<IngestionJob> pageResult = new PageResult<>(
                Collections.emptyList(), 0L, 0, 20, 0, false, false
        );

        when(listJobsUseCase.execute(any(), anyInt(), anyInt())).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/jobs")
                .param("page", "0")
                .param("size", "20")
                .param("status", "RUNNING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
