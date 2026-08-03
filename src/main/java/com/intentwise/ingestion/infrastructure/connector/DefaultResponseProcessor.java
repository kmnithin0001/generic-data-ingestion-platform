package com.intentwise.ingestion.infrastructure.connector;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intentwise.ingestion.domain.model.ProcessingResult;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import com.intentwise.ingestion.domain.service.ResponseProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of ResponseProcessor.
 * Parses raw JSON strings using Jackson pointers configured dynamically
 * in the pagination configuration (e.g., recordsPath, nextPageTokenPath).
 */
@Component
@RequiredArgsConstructor
public class DefaultResponseProcessor implements ResponseProcessor {

    private final ObjectMapper objectMapper;

    @Override
    public ProcessingResult process(RequestExecutionResult result, Map<String, Object> paginationConfig) {
        if (paginationConfig == null) {
            paginationConfig = Collections.emptyMap();
        }
        String body = result.getResponseBody();
        if (body == null || body.trim().isEmpty()) {
            return new ProcessingResult(Collections.emptyList(), null, 0, false, Collections.emptyMap());
        }

        try {
            JsonNode rootNode = objectMapper.readTree(body);

            // 1. Extract Records array
            String recordsPath = (String) paginationConfig.getOrDefault("recordsPath", "");
            JsonNode recordsNode;
            if (recordsPath == null || recordsPath.trim().isEmpty() || "/".equals(recordsPath.trim())) {
                recordsNode = rootNode;
            } else {
                recordsNode = rootNode.at(recordsPath);
            }

            List<Map<String, Object>> records = new ArrayList<>();
            if (recordsNode.isArray()) {
                for (JsonNode element : recordsNode) {
                    Map<String, Object> record = objectMapper.convertValue(element, new TypeReference<Map<String, Object>>() {});
                    records.add(record);
                }
            } else if (recordsNode.isObject() && !recordsNode.isEmpty()) {
                Map<String, Object> record = objectMapper.convertValue(recordsNode, new TypeReference<Map<String, Object>>() {});
                records.add(record);
            }

            // 2. Extract Next Page Token
            String nextPageToken = null;
            String nextTokenPath = (String) paginationConfig.get("nextPageTokenPath");
            if (nextTokenPath != null && !nextTokenPath.trim().isEmpty()) {
                JsonNode tokenNode = rootNode.at(nextTokenPath);
                if (!tokenNode.isMissingNode() && !tokenNode.isNull()) {
                    nextPageToken = tokenNode.asText();
                }
            }

            // 3. Extract Total Records
            int totalRecords = records.size();
            String totalPath = (String) paginationConfig.get("totalRecordsPath");
            if (totalPath != null && !totalPath.trim().isEmpty()) {
                JsonNode totalNode = rootNode.at(totalPath);
                if (!totalNode.isMissingNode() && totalNode.isNumber()) {
                    totalRecords = totalNode.asInt();
                }
            }

            // 4. Determine hasNextPage flag
            boolean hasNextPage = false;
            String hasNextPagePath = (String) paginationConfig.get("hasNextPagePath");
            if (hasNextPagePath != null && !hasNextPagePath.trim().isEmpty()) {
                JsonNode hasNextNode = rootNode.at(hasNextPagePath);
                if (!hasNextNode.isMissingNode()) {
                    hasNextPage = hasNextNode.asBoolean();
                }
            } else {
                if (nextPageToken != null && !nextPageToken.trim().isEmpty()) {
                    hasNextPage = true;
                } else if (paginationConfig.containsKey("pageSize")) {
                    int pageSize = ((Number) paginationConfig.get("pageSize")).intValue();
                    hasNextPage = records.size() >= pageSize;
                }
            }

            return new ProcessingResult(records, nextPageToken, totalRecords, hasNextPage, Collections.emptyMap());

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse and extract JSON response payload", e);
        }
    }
}
