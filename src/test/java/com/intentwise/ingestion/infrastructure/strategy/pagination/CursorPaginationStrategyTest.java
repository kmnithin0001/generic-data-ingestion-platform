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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CursorPaginationStrategyTest {

    private final CursorPaginationStrategy strategy = new CursorPaginationStrategy();

    @Test
    void shouldReturnCursorType() {
        assertEquals(PaginationType.CURSOR, strategy.getType());
    }

    @Test
    void shouldNotApplyCursorOnFirstPage() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("cursorParam", "cursor"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, null, headers, queryParams);

        assertTrue(headers.isEmpty());
        assertTrue(queryParams.isEmpty());
        assertNull(context.getCurrentPageState());
    }

    @Test
    void shouldApplyCursorToQueryParams() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("cursorParam", "starting_after", "placement", "QUERY"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        ProcessingResult lastResult = new ProcessingResult(
                Collections.singletonList(Map.of("id", 123)), "token-abc", 1, true, Collections.emptyMap()
        );

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, lastResult, headers, queryParams);

        assertEquals(List.of("token-abc"), queryParams.get("starting_after"));
        assertTrue(headers.isEmpty());
        assertEquals("token-abc", context.getCurrentPageState());
    }

    @Test
    void shouldApplyCursorToHeaders() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("cursorParam", "X-Cursor", "placement", "HEADER"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        ProcessingResult lastResult = new ProcessingResult(
                Collections.singletonList(Map.of("id", 123)), "token-xyz", 1, true, Collections.emptyMap()
        );

        Map<String, List<String>> headers = new HashMap<>();
        Map<String, List<String>> queryParams = new HashMap<>();

        strategy.setupNextPage(context, lastResult, headers, queryParams);

        assertEquals(List.of("token-xyz"), headers.get("X-Cursor"));
        assertTrue(queryParams.isEmpty());
        assertEquals("token-xyz", context.getCurrentPageState());
    }

    @Test
    void shouldVerifyHasNextPageCorrectly() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("cursorParam", "cursor"))
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        ProcessingResult lastResultWithCursor = new ProcessingResult(
                Collections.singletonList(Map.of("id", 123)), "next-token", 1, true, Collections.emptyMap()
        );
        assertTrue(strategy.hasNextPage(context, lastResultWithCursor));

        ProcessingResult lastResultNoCursor = new ProcessingResult(
                Collections.singletonList(Map.of("id", 123)), null, 1, true, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, lastResultNoCursor));

        ProcessingResult lastResultExplicitFalse = new ProcessingResult(
                Collections.singletonList(Map.of("id", 123)), "next-token", 1, false, Collections.emptyMap()
        );
        assertFalse(strategy.hasNextPage(context, lastResultExplicitFalse));
    }

    @Test
    void shouldThrowOnInvalidConfiguration() {
        SourceConfiguration source = SourceConfiguration.builder()
                .paginationConfig(Map.of("placement", "QUERY")) // missing cursorParam
                .build();
        IngestionContext context = IngestionContext.builder().sourceConfig(source).build();

        assertThrows(IllegalArgumentException.class, () ->
                strategy.setupNextPage(context, null, new HashMap<>(), new HashMap<>())
        );
    }
}
