package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pagination strategy for Next URL endpoints.
 * Stores the full URL returned by the previous payload as `currentPageState`.
 */
@Component
public class NextUrlPaginationStrategy implements PaginationStrategy {

    @Override
    public void setupNextPage(IngestionContext context, ProcessingResult lastProcessingResult, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        if (lastProcessingResult == null) {
            return; // First request uses default base configuration
        }

        String nextUrl = lastProcessingResult.nextPageToken();
        if (nextUrl == null || nextUrl.trim().isEmpty()) {
            return;
        }

        // For Next URL pagination, query parameters are already baked into the next URL.
        // Therefore, we clear any default query parameters to avoid duplication conflicts.
        queryParams.clear();

        // Save next URL as active execution page state
        context.setCurrentPageState(nextUrl);
    }

    @Override
    public boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult) {
        if (lastProcessingResult == null) {
            return true; // First page
        }

        String nextUrl = lastProcessingResult.nextPageToken();
        return lastProcessingResult.hasNextPage() && nextUrl != null && !nextUrl.trim().isEmpty() && nextUrl.startsWith("http");
    }

    @Override
    public PaginationType getType() {
        return PaginationType.NEXT_URL;
    }
}
