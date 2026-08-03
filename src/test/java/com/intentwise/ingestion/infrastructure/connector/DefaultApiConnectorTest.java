package com.intentwise.ingestion.infrastructure.connector;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.service.HttpClient;
import com.intentwise.ingestion.domain.service.factory.AuthenticationRegistry;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultApiConnectorTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private AuthenticationRegistry authenticationRegistry;

    @Mock
    private AuthenticationStrategy authenticationStrategy;

    private DefaultApiConnector connector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        connector = new DefaultApiConnector(httpClient, authenticationRegistry);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFetchPageSuccessfullyCoordinatingOptionsAndAuth() {
        SourceConfiguration source = SourceConfiguration.builder()
                .url("https://api.example.com/v1/resource")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.API_KEY)
                .paginationType(PaginationType.NONE)
                .requestOptions(Map.of(
                        "headers", Map.of("X-App-Id", "ingest-app"),
                        "queryParams", Map.of("version", "2")
                ))
                .build();

        IngestionContext context = IngestionContext.builder()
                .sourceConfig(source)
                .build();

        RequestExecutionResult expectedResult = RequestExecutionResult.builder()
                .statusCode(200)
                .responseBody("{}")
                .executionTime(Duration.ofMillis(100))
                .build();

        when(authenticationRegistry.getStrategy(AuthenticationType.API_KEY)).thenReturn(authenticationStrategy);
        when(httpClient.execute(any(), any(), any(), any(), any(), any())).thenReturn(expectedResult);

        RequestExecutionResult actualResult = connector.fetchPage(context);

        assertEquals(expectedResult, actualResult);

        // Verify request assembly args
        ArgumentCaptor<Map<String, List<String>>> headersCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Map<String, List<String>>> queriesCaptor = ArgumentCaptor.forClass(Map.class);

        verify(authenticationStrategy).authenticate(eq(context), headersCaptor.capture(), queriesCaptor.capture());
        verify(httpClient).execute(
                eq("https://api.example.com/v1/resource"),
                eq("GET"),
                eq(headersCaptor.getValue()),
                eq(queriesCaptor.getValue()),
                eq(null),
                any(Duration.class)
        );

        assertEquals(List.of("ingest-app"), headersCaptor.getValue().get("X-App-Id"));
        assertEquals(List.of("2"), queriesCaptor.getValue().get("version"));
    }
}
