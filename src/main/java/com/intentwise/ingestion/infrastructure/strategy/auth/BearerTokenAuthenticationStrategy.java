package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.BearerTokenConfig;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Authentication strategy that appends the standard 'Authorization: Bearer <token>' header
 * to outbound requests.
 */
@Component
public class BearerTokenAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public void authenticate(IngestionContext context, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> configMap = context.getSourceConfig().getAuthConfig();
        if (configMap == null) {
            throw new IllegalArgumentException("Bearer authentication configuration must not be null");
        }

        String token = (String) configMap.get("token");

        // Validates credentials through constructor invariants
        BearerTokenConfig config = new BearerTokenConfig(token);

        headers.put("Authorization", List.of("Bearer " + config.token()));
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.BEARER;
    }
}
