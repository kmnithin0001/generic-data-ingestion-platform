package com.intentwise.ingestion.domain.model;

/**
 * Enumerates the REST API pagination styles supported by the ingestion system.
 */
public enum PaginationType {
    /**
     * The API endpoint returns all data in a single request. No paging.
     */
    NONE,

    /**
     * Traditional offset-based paging using parameter limits and database offsets.
     */
    LIMIT_OFFSET,

    /**
     * Page number-based paging specifying page index (e.g., page=1, page=2).
     */
    PAGE_NUMBER,

    /**
     * Modern cursor-based paging using a secure opaque token for fetching successive sets.
     */
    CURSOR,

    /**
     * Pagination strategy that reads the full URL of the next page directly from the response body/headers.
     */
    NEXT_URL
}
