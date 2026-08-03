package com.intentwise.ingestion.domain.service.strategy;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.IngestionContext;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface for adding authentication credentials to outbound API requests.
 * Implementations are registered dynamically and resolved via the AuthenticationStrategyRegistry.
 */
public interface AuthenticationStrategy {

    /**
     * Decorates the request headers or query parameters with the necessary security details.
     *
     * @param context     the IngestionContext of the active execution
     * @param headers     the map of request headers to populate
     * @param queryParams the map of request query parameters to populate
     */
    void authenticate(IngestionContext context, Map<String, List<String>> headers, Map<String, List<String>> queryParams);

    /**
     * Returns the authentication type supported by this strategy.
     *
     * @return the AuthenticationType
     */
    AuthenticationType getType();
}
