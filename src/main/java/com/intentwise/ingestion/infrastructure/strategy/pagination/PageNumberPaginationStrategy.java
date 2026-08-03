package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pagination strategy managing traditional Page Number queries (e.g., page=1, page=2).
 * Advances page indexes: next_page = current_page + 1.
 */
@Component
public class PageNumberPaginationStrategy implements PaginationStrategy {

    private static final String PAGE_PARAM = "pageParam";
    private static final String PAGE_SIZE_PARAM = "pageSizeParam";
    private static final String PAGE_SIZE = "pageSize";
    private static final String START_PAGE = "startPage";

    @Override
    public void setupNextPage(IngestionContext context, ProcessingResult lastProcessingResult, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> config = context.getSourceConfig().getPaginationConfig();
        validateConfig(config);

        String pageParam = (String) config.get(PAGE_PARAM);
        int startPage = ((Number) config.get(START_PAGE)).intValue();

        // Calculate next page index
        int nextPage;
        if (lastProcessingResult == null) {
            nextPage = startPage;
        } else {
            nextPage = context.getCurrentPageNumber() + 1;
        }

        // Apply page to query parameters
        queryParams.put(pageParam, List.of(String.valueOf(nextPage)));

        // Optionally apply page size if configured
        if (config.containsKey(PAGE_SIZE_PARAM) && config.get(PAGE_SIZE_PARAM) instanceof String sizeParam) {
            int pageSize = ((Number) config.get(PAGE_SIZE)).intValue();
            queryParams.put(sizeParam, List.of(String.valueOf(pageSize)));
        }

        // Save current page state in context
        context.setCurrentPageNumber(nextPage);
    }

    @Override
    public boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult) {
        if (lastProcessingResult == null) {
            return true; // First page
        }

        Map<String, Object> config = context.getSourceConfig().getPaginationConfig();
        validateConfig(config);
        int pageSize = ((Number) config.get(PAGE_SIZE)).intValue();

        // End if the API indicates no next page, or if returned items are less than pageSize
        return lastProcessingResult.hasNextPage() && lastProcessingResult.records().size() >= pageSize;
    }

    @Override
    public PaginationType getType() {
        return PaginationType.PAGE_NUMBER;
    }

    private void validateConfig(Map<String, Object> config) {
        if (config == null) {
            throw new IllegalArgumentException("Pagination configuration is missing");
        }
        if (!config.containsKey(PAGE_PARAM) || !(config.get(PAGE_PARAM) instanceof String)) {
            throw new IllegalArgumentException("Page parameter name ('pageParam') is missing or invalid");
        }
        if (!config.containsKey(START_PAGE) || !(config.get(START_PAGE) instanceof Number)) {
            throw new IllegalArgumentException("Start page index ('startPage') is missing or invalid");
        }
        if (config.containsKey(PAGE_SIZE_PARAM) && (!config.containsKey(PAGE_SIZE) || !(config.get(PAGE_SIZE) instanceof Number))) {
            throw new IllegalArgumentException("Page size ('pageSize') is required when page size parameter is configured");
        }
    }
}
