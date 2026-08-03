package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageNumberPaginationStrategyTest {

    private final PageNumberPaginationStrategy strategy = new PageNumberPaginationStrategy();

    @Test
    void shouldReturnPageNumberType() {
        assertEquals(PaginationType.PAGE_NUMBER, strategy.getType());
    }

    @Test
    void shouldSetupInitialPage() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("pageParam", "page", "startPage", 1))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, null, headers, queryParams);

        assertEquals(List.of("1"), queryParams.get("page"));
        assertEquals(1, context.getCurrentPageNumber());
    }

    @Test
    void shouldSetupSubsequentPages() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("pageParam", "page", "startPage", 1, "pageSizeParam", "size", "pageSize", 20))
                .build();
        IngestionContext context = IngestionContext.builder()
                .sourceConfig(source)
                .currentPageNumber(2)
                .build();

        ProcessingResult lastResult = new ProcessingResult(
                Collections.nCopies(20, Map.of("key", "val")), null, 20, true, Collections.emptyMap()
        );

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, lastResult, headers, queryParams);

        assertEquals(List.of("3"), queryParams.get("page")); // 2 + 1
        assertEquals(List.of("20"), queryParams.get("size"));
        assertEquals(3, context.getCurrentPageNumber());
    }

    @Test
    void shouldValidateHasNextPageCorrectly() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("pageParam", "page", "startPage", 1, "pageSize", 20))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        ProcessingResult lastFull = new ProcessingResult(
                Collections.nCopies(20, Map.of("k", "v")), null, 20, true, Collections.emptyMap()
        );
        assertTrue(strategy.hasNextPage(context, lastFull));

        ProcessingResult lastPartial = new ProcessingResult(
                Collections.nCopies(15, Map.of("k", "v")), null, 15, true, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, lastPartial));
    }

    @Test
    void shouldThrowOnInvalidConfiguration() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("pageParam", "page")) // missing startPage
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.setupNextPage(context, null, new HashMap<>(), new HashMap<>())
        );
    }
}
