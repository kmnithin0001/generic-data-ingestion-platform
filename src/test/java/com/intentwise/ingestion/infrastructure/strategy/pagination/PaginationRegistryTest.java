package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.service.factory.PaginationRegistry;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationRegistryTest {

    private final PaginationStrategy noneStrategy = new NonePaginationStrategy();
    private final PaginationStrategy limitOffsetStrategy = new LimitOffsetPaginationStrategy();
    private final PaginationStrategy pageNumberStrategy = new PageNumberPaginationStrategy();
    private final PaginationStrategy cursorStrategy = new CursorPaginationStrategy();
    private final PaginationStrategy nextUrlStrategy = new NextUrlPaginationStrategy();

    private final PaginationRegistry registry = new PaginationRegistryImpl(
            List.of(noneStrategy, limitOffsetStrategy, pageNumberStrategy, cursorStrategy, nextUrlStrategy)
    );

    @Test
    void shouldResolveConfiguredStrategies() {
        assertEquals(noneStrategy, registry.getStrategy(PaginationType.NONE));
        assertEquals(limitOffsetStrategy, registry.getStrategy(PaginationType.LIMIT_OFFSET));
        assertEquals(pageNumberStrategy, registry.getStrategy(PaginationType.PAGE_NUMBER));
        assertEquals(cursorStrategy, registry.getStrategy(PaginationType.CURSOR));
        assertEquals(nextUrlStrategy, registry.getStrategy(PaginationType.NEXT_URL));
    }

    @Test
    void shouldThrowExceptionForUnsupportedStrategy() {
        PaginationRegistry partialRegistry = new PaginationRegistryImpl(List.of(noneStrategy));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                partialRegistry.getStrategy(PaginationType.LIMIT_OFFSET)
        );

        assertNotNull(exception.getMessage());
    }
}
