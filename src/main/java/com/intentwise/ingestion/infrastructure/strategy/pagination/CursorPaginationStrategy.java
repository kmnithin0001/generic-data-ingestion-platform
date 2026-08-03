package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pagination strategy managing Cursor-based queries.
 * Extracts the next page token from ProcessingResult and applies it as a cursor parameter.
 */
@Component
public class CursorPaginationStrategy implements PaginationStrategy {

    private static final String CURSOR_PARAM = "cursorParam";
    private static final String PAGE_SIZE_PARAM = "pageSizeParam";
    private static final String PAGE_SIZE = "pageSize";
    private static final String PLACEMENT = "placement"; // HEADER or QUERY

    @Override
    public void setupNextPage(IngestionContext context, ProcessingResult lastProcessingResult, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> config = context.getSourceConfig().getPaginationConfig();
        validateConfig(config);

        String cursorParam = (String) config.get(CURSOR_PARAM);
        String placement = (String) config.getOrDefault(PLACEMENT, "QUERY");

        // Apply page size parameter if configured
        if (config.containsKey(PAGE_SIZE_PARAM) && config.get(PAGE_SIZE_PARAM) instanceof String sizeParam) {
            int pageSize = ((Number) config.get(PAGE_SIZE)).intValue();
            queryParams.put(sizeParam, List.of(String.valueOf(pageSize)));
        }

        // For the first request, no cursor token exists yet
        if (lastProcessingResult == null) {
            return;
        }

        String nextCursor = lastProcessingResult.nextPageToken();
        if (nextCursor == null || nextCursor.trim().isEmpty()) {
            return;
        }

        // Apply cursor token based on configured placement
        if ("HEADER".equalsIgnoreCase(placement)) {
            headers.put(cursorParam, List.of(nextCursor));
        } else {
            queryParams.put(cursorParam, List.of(nextCursor));
        }

        // Store next cursor state in context
        context.setCurrentPageState(nextCursor);
    }

    @Override
    public boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult) {
        if (lastProcessingResult == null) {
            return true; // First page
        }

        // End pagination if the processing result indicates no next page,
        // or if there is no next page token returned.
        String nextCursor = lastProcessingResult.nextPageToken();
        return lastProcessingResult.hasNextPage() && nextCursor != null && !nextCursor.trim().isEmpty();
    }

    @Override
    public PaginationType getType() {
        return PaginationType.CURSOR;
    }

    private void validateConfig(Map<String, Object> config) {
        if (config == null) {
            throw new IllegalArgumentException("Pagination configuration is missing");
        }
        if (!config.containsKey(CURSOR_PARAM) || !(config.get(CURSOR_PARAM) instanceof String)) {
            throw new IllegalArgumentException("Cursor parameter name ('cursorParam') is missing or invalid");
        }
        if (config.containsKey(PLACEMENT) && !List.of("HEADER", "QUERY").contains(config.get(PLACEMENT).toString().toUpperCase())) {
            throw new IllegalArgumentException("Cursor placement must be either 'HEADER' or 'QUERY'");
        }
    }
}
