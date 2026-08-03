package com.intentwise.ingestion.infrastructure.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultResponseProcessorTest {

    private final DefaultResponseProcessor processor = new DefaultResponseProcessor(new ObjectMapper());

    @Test
    void shouldParseFlatArrayResponse() {
        String payload = "[{\"id\":1,\"title\":\"Post 1\"},{\"id\":2,\"title\":\"Post 2\"}]";
        RequestExecutionResult result = RequestExecutionResult.builder()
                .responseBody(payload)
                .statusCode(200)
                .build();

        // Standard JSONPlaceholder config
        Map<String, Object> paginationConfig = Map.of("pageSize", 2);

        ProcessingResult processingResult = processor.process(result, paginationConfig);

        assertEquals(2, processingResult.records().size());
        assertEquals("Post 1", processingResult.records().get(0).get("title"));
        assertEquals("Post 2", processingResult.records().get(1).get("title"));
        assertTrue(processingResult.hasNextPage()); // since records size (2) matches page size (2)
    }

    @Test
    void shouldParseNestedObjectResponse() {
        String payload = "{\"products\":[{\"id\":101,\"name\":\"Product A\"}],\"total\":100,\"skip\":10}";
        RequestExecutionResult result = RequestExecutionResult.builder()
                .responseBody(payload)
                .statusCode(200)
                .build();

        // Standard DummyJSON config
        Map<String, Object> paginationConfig = Map.of(
                "recordsPath", "/products",
                "nextPageTokenPath", "/skip",
                "totalRecordsPath", "/total",
                "pageSize", 1
        );

        ProcessingResult processingResult = processor.process(result, paginationConfig);

        assertEquals(1, processingResult.records().size());
        assertEquals("Product A", processingResult.records().get(0).get("name"));
        assertEquals("10", processingResult.nextPageToken());
        assertEquals(100, processingResult.totalRecords());
        assertTrue(processingResult.hasNextPage());
    }

    @Test
    void shouldReturnEmptyOnNullOrBlankBody() {
        RequestExecutionResult result = RequestExecutionResult.builder().responseBody("   ").build();
        ProcessingResult processingResult = processor.process(result, Collections.emptyMap());

        assertTrue(processingResult.records().isEmpty());
        assertFalse(processingResult.hasNextPage());
    }
}
