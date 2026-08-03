package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicAuthenticationStrategyTest {

    private final BasicAuthenticationStrategy strategy = new BasicAuthenticationStrategy();

    @Test
    void shouldReturnBasicType() {
        assertEquals(AuthenticationType.BASIC, strategy.getType());
    }

    @Test
    void shouldAuthenticateWithBasicHeader() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("username", "user1", "password", "pass1"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.authenticate(context, headers, queryParams);

        String expectedToken = Base64.getEncoder().encodeToString("user1:pass1".getBytes(StandardCharsets.UTF_8));
        assertEquals(List.of("Basic " + expectedToken), headers.get("Authorization"));
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
    void shouldThrowOnBlankUsernameOrPassword() {
        SourceConfiguration source = SourceConfiguration.builder()
                .authConfig(Map.of("username", "  ", "password", "validPass"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.authenticate(context, new HashMap<>(), new HashMap<>())
        );
    }
}
