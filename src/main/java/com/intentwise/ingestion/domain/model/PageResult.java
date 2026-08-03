package com.intentwise.ingestion.domain.model;

import java.util.List;

/**
 * A generic, framework-independent container for paginated query results.
 * This class isolates the domain layer from Spring Data Page models.
 *
 * @param <T> the type of items in the page
 */
public record PageResult<T>(
    List<T> content,
    long totalElements,
    int page,
    int size,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {}
