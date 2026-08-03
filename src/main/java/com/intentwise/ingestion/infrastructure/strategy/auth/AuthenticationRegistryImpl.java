package com.intentwise.ingestion.infrastructure.strategy.auth;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.service.factory.AuthenticationRegistry;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring-driven implementation of AuthenticationRegistry.
 * Discovers and collects all active AuthenticationStrategy beans automatically,
 * storing them in a highly-optimized EnumMap for runtime lookup.
 */
@Component
public class AuthenticationRegistryImpl implements AuthenticationRegistry {

    private final Map<AuthenticationType, AuthenticationStrategy> strategiesMap;

    /**
     * Constructs the registry by autowiring all discovered strategy beans.
     *
     * @param strategies list of active AuthenticationStrategy beans
     */
    public AuthenticationRegistryImpl(List<AuthenticationStrategy> strategies) {
        this.strategiesMap = new EnumMap<>(AuthenticationType.class);
        for (AuthenticationStrategy strategy : strategies) {
            this.strategiesMap.put(strategy.getType(), strategy);
        }
    }

    @Override
    public AuthenticationStrategy getStrategy(AuthenticationType type) {
        return Optional.ofNullable(strategiesMap.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No authentication strategy found for type: " + type));
    }
}
