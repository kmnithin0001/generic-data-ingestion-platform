package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonePaginationStrategyTest {

    private final NonePaginationStrategy strategy = new NonePaginationStrategy();

    @Test
    void shouldReturnNoneType() {
        assertEquals(PaginationType.NONE, strategy.getType());
    }

    @Test
    void shouldNeverHaveNextPage() {
        IngestionContext context = IngestionContext.builder().build();
        ProcessingResult result = new ProcessingResult(Collections.emptyList(), null, 0, true, Collections.emptyMap());

        assertFalse(strategy.hasNextPage(context, result));
        assertTrue(strategy.hasNextPage(context, null));
    }

    @Test
    void shouldNotModifyRequestHeadersOrQueryParams() {
        IngestionContext context = IngestionContext.builder().build();
        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, null, headers, queryParams);

        assertTrue(headers.isEmpty());
        assertTrue(queryParams.isEmpty());
    }
}
