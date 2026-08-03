package com.intentwise.ingestion.infrastructure.connector;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebClientHttpClientTest {

    private WireMockServer wireMockServer;
    private WebClientHttpClient httpClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        httpClient = new WebClientHttpClient(WebClient.builder());
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldExecuteGetRequestSuccessfully() {
        wireMockServer.stubFor(get(urlEqualTo("/test-get?param=value"))
                .willReturn(aResponse()
                        .withHeader("X-Response-Header", "success")
                        .withBody("{\"data\":\"ok\"}")
                        .withStatus(200)));

        String url = "http://localhost:" + wireMockServer.port() + "/test-get";
        RequestExecutionResult result = httpClient.execute(
                url,
                "GET",
                Map.of("Accept", List.of("application/json")),
                Map.of("param", List.of("value")),
                null,
                Duration.ofSeconds(5)
        );

        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals("{\"data\":\"ok\"}", result.getResponseBody());
        assertEquals("success", result.getHeaders().get("X-Response-Header").get(0));
        assertTrue(result.getResponseSizeBytes() > 0);
        assertTrue(result.getExecutionTime().toMillis() >= 0);
    }

    @Test
    void shouldExecutePostRequestWithBody() {
        wireMockServer.stubFor(post(urlEqualTo("/test-post"))
                .willReturn(aResponse()
                        .withBody("{\"created\":true}")
                        .withStatus(201)));

        String url = "http://localhost:" + wireMockServer.port() + "/test-post";
        RequestExecutionResult result = httpClient.execute(
                url,
                "POST",
                Map.of("Content-Type", List.of("application/json")),
                Collections.emptyMap(),
                "{\"name\":\"john\"}",
                Duration.ofSeconds(5)
        );

        assertNotNull(result);
        assertEquals(201, result.getStatusCode());
        assertEquals("{\"created\":true}", result.getResponseBody());
        assertTrue(result.getRequestSizeBytes() > 0);
    }

    @Test
    void shouldMapErrorStatusesToExecutionResult() {
        wireMockServer.stubFor(get(urlEqualTo("/error-400"))
                .willReturn(aResponse()
                        .withBody("Bad Request Details")
                        .withStatus(400)));

        String url = "http://localhost:" + wireMockServer.port() + "/error-400";
        RequestExecutionResult result = httpClient.execute(
                url,
                "GET",
                Collections.emptyMap(),
                Collections.emptyMap(),
                null,
                Duration.ofSeconds(5)
        );

        assertNotNull(result);
        assertEquals(400, result.getStatusCode());
        assertEquals("Bad Request Details", result.getResponseBody());
    }
}
