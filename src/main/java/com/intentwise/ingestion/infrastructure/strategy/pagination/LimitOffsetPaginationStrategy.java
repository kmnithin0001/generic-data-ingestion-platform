package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pagination strategy managing traditional Limit/Offset queries.
 * Calculates successive offsets: next_offset = current_offset + records_fetched.
 */
@Component
public class LimitOffsetPaginationStrategy implements PaginationStrategy {

    private static final String LIMIT_PARAM = "limitParam";
    private static final String OFFSET_PARAM = "offsetParam";
    private static final String PAGE_SIZE = "pageSize";

    @Override
    public void setupNextPage(IngestionContext context, ProcessingResult lastProcessingResult, Map<String, List<String>> headers, Map<String, List<String>> queryParams) {
        Map<String, Object> config = context.getSourceConfig().getPaginationConfig();
        validateConfig(config);

        String limitParam = (String) config.get(LIMIT_PARAM);
        String offsetParam = (String) config.get(OFFSET_PARAM);
        int pageSize = ((Number) config.get(PAGE_SIZE)).intValue();

        // Retrieve current offset (defaults to 0 for initial page)
        int currentOffset = 0;
        if (context.getCurrentPageState() != null) {
            currentOffset = Integer.parseInt(context.getCurrentPageState());
        }

        // Apply to query parameters
        queryParams.put(limitParam, List.of(String.valueOf(pageSize)));
        queryParams.put(offsetParam, List.of(String.valueOf(currentOffset)));

        // Prepare next state for the subsequent run
        int nextOffset = currentOffset + (lastProcessingResult != null ? lastProcessingResult.records().size() : 0);
        context.setCurrentPageState(String.valueOf(nextOffset));
    }

    @Override
    public boolean hasNextPage(IngestionContext context, ProcessingResult lastProcessingResult) {
        if (lastProcessingResult == null) {
            return true; // First page
        }

        Map<String, Object> config = context.getSourceConfig().getPaginationConfig();
        validateConfig(config);
        int pageSize = ((Number) config.get(PAGE_SIZE)).intValue();

        // End pagination if the API processor explicitly indicates no more pages,
        // or if the record count returned is less than the requested page size (standard EOF check).
        if (!lastProcessingResult.hasNextPage() || lastProcessingResult.records().size() < pageSize) {
            return false;
        }

        // Optional maximum records check
        if (config.containsKey("maxRecords")) {
            int maxRecords = ((Number) config.get("maxRecords")).intValue();
            return context.getTotalRecordsFetched() < maxRecords;
        }

        return true;
    }

    @Override
    public PaginationType getType() {
        return PaginationType.LIMIT_OFFSET;
    }

    private void validateConfig(Map<String, Object> config) {
        if (config == null) {
            throw new IllegalArgumentException("Pagination configuration is missing");
        }
        if (!config.containsKey(LIMIT_PARAM) || !(config.get(LIMIT_PARAM) instanceof String)) {
            throw new IllegalArgumentException("Limit parameter name ('limitParam') is missing or invalid");
        }
        if (!config.containsKey(OFFSET_PARAM) || !(config.get(OFFSET_PARAM) instanceof String)) {
            throw new IllegalArgumentException("Offset parameter name ('offsetParam') is missing or invalid");
        }
        if (!config.containsKey(PAGE_SIZE) || !(config.get(PAGE_SIZE) instanceof Number)) {
            throw new IllegalArgumentException("Page size ('pageSize') is missing or invalid");
        }
    }
}
