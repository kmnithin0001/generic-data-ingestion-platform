package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.ApiKeyConfig;
import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Authentication strategy that appends an API Key to either the request headers or query parameters
 * based on configuration options.
 */
@Component
public class ApiKeyAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public void authenticate(IngestionContext context, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> configMap = context.getSourceConfig().getAuthConfig();
        if (configMap == null) {
            throw new IllegalArgumentException("API Key authentication configuration must not be null");
        }

        String keyName = (String) configMap.get("keyName");
        String keyValue = (String) configMap.get("keyValue");
        String placement = (String) configMap.get("placement");

        // Validates credentials through constructor invariants
        ApiKeyConfig config = new ApiKeyConfig(keyName, keyValue, placement);

        if ("HEADER".equalsIgnoreCase(config.placement())) {
            headers.put(config.keyName(), List.of(config.keyValue()));
        } else {
            queryParams.put(config.keyName(), List.of(config.keyValue()));
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.API_KEY;
    }
}
