package com.intentwise.ingestion.application.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.intentwise.ingestion.BaseIntegrationTest;
import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.repository.StorageService;
import com.intentwise.ingestion.infrastructure.persistence.repository.RawApiResponseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngestionEngineIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IngestionEngine ingestionEngine;

    @Autowired
    private StorageService storageService;

    @Autowired
    private RawApiResponseRepository rawApiResponseRepository;

    private WireMockServer wireMockServer;

    @BeforeEach
    void startWireMock() {
        wireMockServer = new WireMockServer(0); // Dynamic port allocation
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldIngestFlatArrayApiSuccessfully() {
        // Stub flat array endpoint (JSONPlaceholder style)
        wireMockServer.stubFor(get(urlEqualTo("/posts"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\": 1, \"title\": \"Mock Post 1\"}, {\"id\": 2, \"title\": \"Mock Post 2\"}]")
                        .withStatus(200)));

        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .name("Wiremock Posts API")
                .url("http://localhost:" + wireMockServer.port() + "/posts")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.NONE)
                .paginationConfig(Map.of("pageSize", 10))
                .build();

        source = storageService.saveSource(source);
        IngestionJob job = ingestionEngine.ingest(source);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(2, job.getTotalRecordsFetched());
        assertEquals(1, job.getTotalPagesFetched());

        // Check if raw response was persisted
        var rawResponses = rawApiResponseRepository.findAll().stream()
                .filter(res -> res.getJob().getId().equals(job.getId()))
                .toList();
        assertEquals(1, rawResponses.size());
        assertTrue(rawResponses.get(0).getResponseBody().contains("Mock Post 1"));
    }

    @Test
    void shouldIngestNestedProductApiSuccessfully() {
        // Stub nested object endpoint (DummyJSON style)
        wireMockServer.stubFor(get(urlPathEqualTo("/products"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"products\": [{\"id\": 10, \"title\": \"Product 1\"}, {\"id\": 11, \"title\": \"Product 2\"}], \"total\": 2, \"skip\": 0}")
                        .withStatus(200)));

        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .name("Wiremock Products API")
                .url("http://localhost:" + wireMockServer.port() + "/products")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.LIMIT_OFFSET)
                .paginationConfig(Map.of(
                        "limitParam", "limit",
                        "offsetParam", "skip",
                        "pageSize", 2,
                        "recordsPath", "/products",
                        "totalRecordsPath", "/total",
                        "maxRecords", 2
                ))
                .build();

        source = storageService.saveSource(source);
        IngestionJob job = ingestionEngine.ingest(source);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(2, job.getTotalRecordsFetched());
        assertEquals(1, job.getTotalPagesFetched());
    }

    @Test
    void shouldRetryOnTransientFailureAndSucceed() {
        // First request: 503 error, Second request: 200 OK success
        wireMockServer.stubFor(get(urlEqualTo("/retry"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("Succeeded"));

        wireMockServer.stubFor(get(urlEqualTo("/retry"))
                .inScenario("RetryScenario")
                .whenScenarioStateIs("Succeeded")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\": 99}]")
                        .withStatus(200)));

        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .name("Wiremock Retry API")
                .url("http://localhost:" + wireMockServer.port() + "/retry")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.NONE)
                .build();

        source = storageService.saveSource(source);
        IngestionJob job = ingestionEngine.ingest(source);

        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(1, job.getTotalRecordsFetched());
    }

    @Test
    void shouldFailJobWhenRetryExhausted() {
        // Continuous 500 error
        wireMockServer.stubFor(get(urlEqualTo("/always-fail"))
                .willReturn(aResponse().withStatus(500)));

        SourceConfiguration source = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .name("Wiremock Fail API")
                .url("http://localhost:" + wireMockServer.port() + "/always-fail")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.NONE)
                .paginationType(PaginationType.NONE)
                .build();

        source = storageService.saveSource(source);
        IngestionJob job = ingestionEngine.ingest(source);

        assertEquals(JobStatus.FAILED, job.getStatus());
        assertNotNull(job.getErrorMessage());
    }
}
