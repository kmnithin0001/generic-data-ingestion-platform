package com.intentwise.ingestion.domain.service.factory;

import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;

/**
 * Domain-level registry interface for resolving pagination strategies based on paging architecture.
 */
public interface PaginationRegistry {

    /**
     * Resolves the matching PaginationStrategy implementation.
     *
     * @param type the PaginationType paging scheme
     * @return the PaginationStrategy implementation
     * @throws IllegalArgumentException if the strategy is unsupported
     */
    PaginationStrategy getStrategy(PaginationType type);
}
