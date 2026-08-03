package com.intentwise.ingestion.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Context object carrying state through the entire ingestion pipeline run.
 * Captures initial configurations, execution trackers, batch buffer, and statistics.
 */
@Getter
@Setter
@Builder
public class IngestionContext {
    private final UUID correlationId;
    private final SourceConfiguration sourceConfig;
    private IngestionJob job;
    
    private String currentPageState;
    private int currentPageNumber;
    private int totalRecordsFetched;
    private int totalBytesFetched;
    private int retryCount;

    @Builder.Default
    private final List<RawApiResponse> bufferedResponses = new ArrayList<>();

    /**
     * Appends a raw response to the in-memory batch buffer.
     *
     * @param response the RawApiResponse to buffer
     */
    public void bufferResponse(RawApiResponse response) {
        if (response != null) {
            this.bufferedResponses.add(response);
        }
    }

    /**
     * Clears the in-memory response buffer once written to storage.
     */
    public void clearResponseBuffer() {
        this.bufferedResponses.clear();
    }

    /**
     * Increments the retry count tracker.
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
