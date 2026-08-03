package com.intentwise.ingestion.domain.service.strategy;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;

import java.util.List;
import java.util.Map;

/**
 * Strategy interface to calculate pagination state and update outbound request arguments.
 * Implementations are registered dynamically and resolved via the PaginationStrategyRegistry.
 */
public interface PaginationStrategy {

    /**
     * Updates the query parameters or request headers for the subsequent page fetch based on the previous page's result.
     *
     * @param context              the active IngestionContext
     * @param lastProcessingResult the ProcessingResult of the last page processed
     * @param headers              the map of request headers to populate
     * @param queryParams          the map of request query parameters to populate
     */
    void setupNextPage(
        IngestionContext context,
        ProcessingResult lastProcessingResult,
        Map<String, List<String>> headers,
        Map<String, List<String>> queryParams
    );

    /**
     * Inspects the context and the last page's processing result to decide if a next page exists and should be queried.
     *
     * @param context              the active IngestionContext
     * @param lastProcessingResult the ProcessingResult of the last page processed
     * @return true if another page should be fetched, false otherwise
     */
    boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult);

    /**
     * Returns the pagination type supported by this strategy.
     *
     * @return the PaginationType
     */
    PaginationType getType();
}
