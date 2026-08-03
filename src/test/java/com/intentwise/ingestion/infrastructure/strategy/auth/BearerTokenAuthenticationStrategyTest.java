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

class BearerTokenAuthenticationStrategyTest {

    private final BearerTokenAuthenticationStrategy strategy = new BearerTokenAuthenticationStrategy();

    @Test
    void shouldReturnBearerType() {
        assertEquals(AuthenticationType.BEARER, strategy.getType());
    }

    @Test
    void shouldAuthenticateWithBearerHeader() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("token", "super-secret-token"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.authenticate(context, headers, queryParams);

        assertEquals(List.of("Bearer super-secret-token"), headers.get("Authorization"));
        assertTrue(queryParams.isEmpty());
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
    void shouldThrowOnBlankToken() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("token", "   "))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.authenticate(context, new HashMap<>(), new HashMap<>())
        );
    }
}
