package com.intentwise.ingestion.domain.service.factory;

import com.intentwise.ingestion.domain.service.ApiConnector;

/**
 * Domain-level registry interface for resolving ApiConnector implementations based on type.
 */
public interface ConnectorRegistry {

    /**
     * Resolves the matching ApiConnector implementation.
     *
     * @param type the connector type name (e.g., "REST")
     * @return the ApiConnector implementation
     * @throws IllegalArgumentException if the connector type is unsupported
     */
    ApiConnector getConnector(String type);
}
