package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyAuthenticationStrategyTest {

    private final ApiKeyAuthenticationStrategy strategy = new ApiKeyAuthenticationStrategy();

    @Test
    void shouldReturnApiKeyType() {
        assertEquals(AuthenticationType.API_KEY, strategy.getType());
    }

    @Test
    void shouldAuthenticateInHeader() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("keyName", "X-API-KEY", "keyValue", "token-123", "placement", "HEADER"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.authenticate(context, headers, queryParams);

        assertEquals(List.of("token-123"), headers.get("X-API-KEY"));
        assertTrue(queryParams.isEmpty());
    }

    @Test
    void shouldAuthenticateInQuery() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("keyName", "api_key", "keyValue", "token-456", "placement", "QUERY"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.authenticate(context, headers, queryParams);

        assertEquals(List.of("token-456"), queryParams.get("api_key"));
        assertTrue(headers.isEmpty());
    }

    @Test
    void shouldThrowOnNullConfig() {
        SourceConfiguration source = SourceConfiguration.builder().authConfig(null).build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.authenticate(context, new HashMap<>(), new HashMap<>())
        );
    }

    @Test
    void shouldThrowOnInvalidPlacement() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("keyName", "key", "keyValue", "val", "placement", "BODY"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.authenticate(context, new HashMap<>(), new HashMap<>())
        );
    }
}
