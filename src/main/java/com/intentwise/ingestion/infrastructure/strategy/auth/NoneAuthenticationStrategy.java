package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Authentication strategy for public endpoints that require no security headers or query parameters.
 */
@Component
public class NoneAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public void authenticate(IngestionContext context, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        // No authentication required, request remains untouched.
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.NONE;
    }
}
