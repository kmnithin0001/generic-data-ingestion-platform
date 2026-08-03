package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.BasicAuthConfig;
import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Authentication strategy that constructs a Base64-encoded username/password authorization header
 * ('Authorization: Basic <credentials>') and appends it to outbound requests.
 */
@Component
public class BasicAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public void authenticate(IngestionContext context, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> configMap = context.getSourceConfig().getAuthConfig();
        if (configMap == null) {
            throw new IllegalArgumentException("Basic authentication configuration must not be null");
        }

        String username = (String) configMap.get("username");
        String password = (String) configMap.get("password");

        // Validates credentials through constructor invariants
        BasicAuthConfig config = new BasicAuthConfig(username, password);

        String rawCredentials = config.username() + ":" + config.password();
        String encodedCredentials = Base64.getEncoder().encodeToString(rawCredentials.getBytes(StandardCharsets.UTF_8));

        headers.put("Authorization", List.of("Basic " + encodedCredentials));
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.BASIC;
    }
}
