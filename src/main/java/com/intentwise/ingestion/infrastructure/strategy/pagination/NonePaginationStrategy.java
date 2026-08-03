package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pagination strategy for endpoints returning all results in a single call.
 */
@Component
public class NonePaginationStrategy implements PaginationStrategy {

    @Override
    public void setupNextPage(IngestionContext context, ProcessingResult lastProcessingResult, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        // No pagination setup required.
    }

    @Override
    public boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult) {
        return lastProcessingResult == null;
    }

    @Override
    public PaginationType getType() {
        return PaginationType.NONE;
    }
}
