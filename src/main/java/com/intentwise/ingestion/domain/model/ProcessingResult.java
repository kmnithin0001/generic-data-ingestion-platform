package com.intentwise.ingestion.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates the results of parsing an API payload, extracting the data records
 * and extracting pagination hints to instruct the pipeline's iteration.
 *
 * @param records         extracted individual record maps
 * @param nextPageToken   extracted token or offset identifier for the next page, if present
 * @param totalRecords    count of records parsed in this payload
 * @param hasNextPage     whether pagination indicates another page exists
 * @param customMetadata  additional context parsed from response headers or payloads
 */
public record ProcessingResult(
    List<Map<String, Object>> records,
    String nextPageToken,
    int totalRecords,
    boolean hasNextPage,
    Map<String, Object> customMetadata
) {}
