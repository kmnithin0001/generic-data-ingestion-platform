package com.intentwise.ingestion.infrastructure.strategy.pagination;

import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.service.factory.PaginationRegistry;
import com.intentwise.ingestion.domain.service.strategy.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring-driven implementation of PaginationRegistry.
 * Discovers and collects all active PaginationStrategy beans automatically,
 * storing them in a highly-optimized EnumMap for runtime lookup.
 */
@Component
public class PaginationRegistryImpl implements PaginationRegistry {

    private final Map<PaginationType, PaginationStrategy> strategiesMap;

    /**
     * Constructs the registry by autowiring all discovered strategy beans.
     *
     * @param strategies list of active PaginationStrategy beans
     */
    public PaginationRegistryImpl(List<PaginationStrategy> strategies) {
        this.strategiesMap = new EnumMap<>(PaginationType.class);
        for (PaginationStrategy strategy : strategies) {
            this.strategiesMap.put(strategy.getType(), strategy);
        }
    }

    @Override
    public PaginationStrategy getStrategy(PaginationType type) {
        return Optional.ofNullable(strategiesMap.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No pagination strategy found for type: " + type));
    }
}
