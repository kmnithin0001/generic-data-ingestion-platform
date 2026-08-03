package com.intentwise.ingestion.domain.service;

import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;

import java.util.Map;

/**
 * Domain-level service interface for parsing raw execution results
 * and extracting records and pagination metadata.
 */
public interface ResponseProcessor {

    /**
     * Parses the response body and extracts records, next page tokens, and counts.
     *
     * @param result           the RequestExecutionResult of the HTTP call
     * @param paginationConfig the pagination configuration containing path extraction parameters
     * @return the ProcessingResult record
     */
    ProcessingResult process(RequestExecutionResult result, Map<String, Object> paginationConfig);
}
