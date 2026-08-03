package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.service.factory.AuthenticationRegistry;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationRegistryTest {

    private final AuthenticationStrategy noneStrategy = new NoneAuthenticationStrategy();
    private final AuthenticationStrategy apiKeyStrategy = new ApiKeyAuthenticationStrategy();
    private final AuthenticationStrategy bearerStrategy = new BearerTokenAuthenticationStrategy();
    private final AuthenticationStrategy basicStrategy = new BasicAuthenticationStrategy();

    private final AuthenticationRegistry registry = new AuthenticationRegistryImpl(
            List.of(noneStrategy, apiKeyStrategy, bearerStrategy, basicStrategy)
    );

    @Test
    void shouldResolveConfiguredStrategies() {
        assertEquals(noneStrategy, registry.getStrategy(AuthenticationType.NONE));
        assertEquals(apiKeyStrategy, registry.getStrategy(AuthenticationType.API_KEY));
        assertEquals(bearerStrategy, registry.getStrategy(AuthenticationType.BEARER));
        assertEquals(basicStrategy, registry.getStrategy(AuthenticationType.BASIC));
    }

    @Test
    void shouldThrowExceptionForUnsupportedStrategy() {
        AuthenticationRegistry partialRegistry = new AuthenticationRegistryImpl(List.of(noneStrategy));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                partialRegistry.getStrategy(AuthenticationType.BASIC)
        );

        assertNotNull(exception.getMessage());
    }
}
