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

class LimitOffsetPaginationStrategyTest {

    private final LimitOffsetPaginationStrategy strategy = new LimitOffsetPaginationStrategy();

    @Test
    void shouldReturnLimitOffsetType() {
        assertEquals(PaginationType.LIMIT_OFFSET, strategy.getType());
    }

    @Test
    void shouldSetupInitialPageParameters() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("limitParam", "size", "offsetParam", "start", "pageSize", 50))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, null, headers, queryParams);

        assertEquals(List.of("50"), queryParams.get("size"));
        assertEquals(List.of("0"), queryParams.get("start"));
        assertEquals("0", context.getCurrentPageState());
    }

    @Test
    void shouldSetupNextPageParameters() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("limitParam", "size", "offsetParam", "start", "pageSize", 50))
                .build();
        IngestionContext context = IngestionContext.builder()
                .sourceConfig(source)
                .currentPageState("0")
                .build();

        ProcessingResult lastResult = new ProcessingResult(
                List.of(Map.of("id", 1), Map.of("id", 2)), null, 2, true, Collections.emptyMap()
        );

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, lastResult, headers, queryParams);

        assertEquals(List.of("50"), queryParams.get("size"));
        assertEquals(List.of("0"), queryParams.get("start"));
        assertEquals("2", context.getCurrentPageState()); // next offset = 0 + 2
    }

    @Test
    void shouldDetermineHasNextPageCorrecty() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("limitParam", "size", "offsetParam", "start", "pageSize", 50))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        // If returned records size matches page size
        ProcessingResult resultFull = new ProcessingResult(
                Collections.nCopies(50, Map.of("key", "val")), null, 50, true, Collections.emptyMap()
        );
        assertTrue(strategy.hasNextPage(context, resultFull));

        // If returned records is less than page size (EOF)
        ProcessingResult resultPartial = new ProcessingResult(
                Collections.nCopies(49, Map.of("key", "val")), null, 49, true, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, resultPartial));

        // If API processor explicitly sets hasNextPage to false
        ProcessingResult resultExplicitFalse = new ProcessingResult(
                Collections.nCopies(50, Map.of("key", "val")), null, 50, false, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, resultExplicitFalse));
    }

    @Test
    void shouldStopAtMaxRecordsLimit() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("limitParam", "size", "offsetParam", "start", "pageSize", 50, "maxRecords", 100))
                .build();
        IngestionContext context = IngestionContext.builder()
                .sourceConfig(source)
                .totalRecordsFetched(100)
                .build();

        ProcessingResult result = new ProcessingResult(
                Collections.nCopies(50, Map.of("key", "val")), null, 50, true, Collections.emptyMap()
        );

        assertFalse(strategy.hasNextPage(context, result));
    }

    @Test
    void shouldThrowOnInvalidConfiguration() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("limitParam", "size")) // missing offset
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.setupNextPage(context, null, new HashMap<>(), new HashMap<>())
        );
    }
}
