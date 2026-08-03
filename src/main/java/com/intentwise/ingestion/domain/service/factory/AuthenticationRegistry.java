package com.intentwise.ingestion.domain.service.factory;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;

/**
 * Domain-level registry interface for resolving authentication strategies based on security type.
 */
public interface AuthenticationRegistry {

    /**
     * Resolves the matching AuthenticationStrategy implementation.
     *
     * @param type the AuthenticationType security scheme
     * @return the AuthenticationStrategy implementation
     * @throws IllegalArgumentException if the strategy is unsupported
     */
    AuthenticationStrategy getStrategy(AuthenticationType type);
}
