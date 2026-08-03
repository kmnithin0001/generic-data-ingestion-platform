package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoneAuthenticationStrategyTest {

    private final NoneAuthenticationStrategy strategy = new NoneAuthenticationStrategy();

    @Test
    void shouldReturnNoneType() {
        assertEquals(AuthenticationType.NONE, strategy.getType());
    }

    @Test
    void shouldNotModifyRequestHeadersOrQueryParams() {
        IngestionContext context = IngestionContext.builder().build();
        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.authenticate(context, headers, queryParams);

        assertTrue(headers.isEmpty());
        assertTrue(queryParams.isEmpty());
    }
}
