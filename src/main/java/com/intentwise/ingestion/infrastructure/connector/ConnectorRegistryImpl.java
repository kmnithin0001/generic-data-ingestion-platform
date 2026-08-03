package com.intentwise.ingestion.infrastructure.connector;

import com.intentwise.ingestion.domain.service.ApiConnector;
import com.intentwise.ingestion.domain.service.factory.ConnectorRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring-driven implementation of ConnectorRegistry.
 * Collects all ApiConnector beans and maps them to their supported type keys.
 */
@Component
public class ConnectorRegistryImpl implements ConnectorRegistry {

    private final Map<String, ApiConnector> registryMap;

    /**
     * Constructs the registry by autowiring all discovered connector beans.
     *
     * @param connectors list of active ApiConnector beans
     */
    public ConnectorRegistryImpl(List<ApiConnector> connectors) {
        this.registryMap = new HashMap<>();
        for (ApiConnector connector : connectors) {
            // Register under REST if supported
            if (connector.supports("REST")) {
                this.registryMap.put("REST", connector);
            }
            // Register other types if the connector class matches standard formats
            String className = connector.getClass().getSimpleName();
            this.registryMap.put(className.toUpperCase(), connector);
        }
    }

    @Override
    public ApiConnector getConnector(String type) {
        String normalizedKey = (type == null || type.trim().isEmpty()) ? "REST" : type.trim().toUpperCase();
        return Optional.ofNullable(registryMap.get(normalizedKey))
                .orElseThrow(() -> new IllegalArgumentException("No connector found for type: " + normalizedKey));
    }
}
