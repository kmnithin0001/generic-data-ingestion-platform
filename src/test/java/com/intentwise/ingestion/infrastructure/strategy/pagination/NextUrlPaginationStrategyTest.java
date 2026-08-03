package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NextUrlPaginationStrategyTest {

    private final NextUrlPaginationStrategy strategy = new NextUrlPaginationStrategy();

    @Test
    void shouldReturnNextUrlType() {
        assertEquals(PaginationType.NEXT_URL, strategy.getType());
    }

    @Test
    void shouldDoNothingOnFirstPage() {
        IngestionContext context = IngestionContext.builder().build();
        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, null, headers, queryParams);

        assertTrue(headers.isEmpty());
        assertTrue(queryParams.isEmpty());
        assertNull(context.getCurrentPageState());
    }

    @Test
    void shouldClearQueryParamsAndSaveNextUrl() {
        IngestionContext context = IngestionContext.builder().build();

        ProcessingResult lastResult = new ProcessingResult(
                Collections.emptyList(), "https://api.example.com/data?page=2&size=10", 0, true, Collections.emptyMap()
        );

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("someParam", new ArrayList<>(List.of("value")));

        strategy.setupNextPage(context, lastResult, headers, queryParams);

        // Parameters should be cleared because they are fully baked into the next URL
        assertTrue(queryParams.isEmpty());
        assertEquals("https://api.example.com/data?page=2&size=10", context.getCurrentPageState());
    }

    @Test
    void shouldVerifyHasNextPageCorrectly() {
        IngestionContext context = IngestionContext.builder().build();

        // Valid http/https URL
        ProcessingResult resultValidUrl = new ProcessingResult(
                Collections.emptyList(), "https://api.example.com/data?page=2", 0, true, Collections.emptyMap()
        );
        assertTrue(strategy.hasNextPage(context, resultValidUrl));

        // Invalid URL protocol
        ProcessingResult resultInvalidUrl = new ProcessingResult(
                Collections.emptyList(), "ftp://api.example.com", 0, true, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, resultInvalidUrl));

        // Missing URL
        ProcessingResult resultNullUrl = new ProcessingResult(
                Collections.emptyList(), null, 0, true, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, resultNullUrl));
    }
}
