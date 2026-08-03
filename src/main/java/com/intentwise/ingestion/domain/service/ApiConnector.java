package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;

/**
 * Interface representing a connector to a specific type of API system (e.g., REST, SOAP).
 * Connectors are registered dynamically and resolved via the ConnectorRegistry.
 */
public interface ApiConnector {

    /**
     * Executes the API call for the current state stored inside the IngestionContext.
     *
     * @param context the IngestionContext of the active job execution
     * @return the RequestExecutionResult
     */
    RequestExecutionResult fetchPage(IngestionContext context);

    /**
     * Identifies if this connector implementation can handle the specified source type.
     *
     * @param type the type string (e.g. "REST", "GRAPHQL")
     * @return true if supported, false otherwise
     */
    boolean supports(String type);
}
