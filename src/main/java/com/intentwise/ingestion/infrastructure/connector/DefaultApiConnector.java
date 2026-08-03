package com.intentwise.ingestion.infrastructure.connector;

import com.intentwise.ingestion.domain.model.IngestionContext;
import com.intentwise.ingestion.domain.model.RequestExecutionResult;
import com.intentwise.ingestion.domain.service.ApiConnector;
import com.intentwise.ingestion.domain.service.HttpClient;
import com.intentwise.ingestion.domain.service.factory.AuthenticationRegistry;
import com.intentwise.ingestion.domain.service.strategy.AuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of ApiConnector.
 * Resolves static request options, triggers the resolved AuthenticationStrategy,
 * delegates pagination overrides, and coordinates request execution.
 */
@Component
@RequiredArgsConstructor
public class DefaultApiConnector implements ApiConnector {

    private final HttpClient httpClient;
    private final AuthenticationRegistry authenticationRegistry;

    @Override
    public RequestExecutionResult fetchPage(IngestionContext context) {
        var sourceConfig = context.getSourceConfig();

        // 1. Resolve target URL (check if a PaginationStrategy stored a custom next URL in the state)
        String url = sourceConfig.getUrl();
        if (context.getCurrentPageState() != null && context.getCurrentPageState().startsWith("http")) {
            url = context.getCurrentPageState();
        }

        // 2. Initialize mutable header and query param maps
        Map<String, List<String>> headers = parseRequestOptionMap(sourceConfig.getRequestOptions(), "headers");
        Map<String, List<String>> queryParams = parseRequestOptionMap(sourceConfig.getRequestOptions(), "queryParams");

        // 3. Apply Authentication Strategy decorations
        AuthenticationStrategy authStrategy = authenticationRegistry.getStrategy(sourceConfig.getAuthType());
        authStrategy.authenticate(context, headers, queryParams);

        // 4. Resolve HttpMethod and body details
        String method = sourceConfig.getMethod().name();
        String body = null;
        if (sourceConfig.getRequestOptions() != null && sourceConfig.getRequestOptions().containsKey("body")) {
            body = sourceConfig.getRequestOptions().get("body").toString();
        }

        // 5. Parse timeouts
        Duration timeout = Duration.ofSeconds(15);
        if (sourceConfig.getRequestOptions() != null && sourceConfig.getRequestOptions().containsKey("timeoutMs")) {
            long ms = ((Number) sourceConfig.getRequestOptions().get("timeoutMs")).longValue();
            timeout = Duration.ofMillis(ms);
        }

        // 6. Execute request
        return httpClient.execute(url, method, headers, queryParams, body, timeout);
    }

    @Override
    public boolean supports(String type) {
        // This is a generic REST connector
        return "REST".equalsIgnoreCase(type) || type == null;
    }

    /**
     * Extracts and converts request options configurations (headers/query parameters) to list maps.
     */
    private Map<String, List<String>> parseRequestOptionMap(Map<String, Object> requestOptions, String key) {
        Map<String, List<String>> result = new HashMap<>();
        if (requestOptions == null || !requestOptions.containsKey(key)) {
            return result;
        }

        Object mapObj = requestOptions.get(key);
        if (mapObj instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (v instanceof List<?> list) {
                    List<String> stringList = list.stream().map(Object::toString).toList();
                    result.put(k.toString(), stringList);
                } else if (v != null) {
                    List<String> list = new ArrayList<>();
                    list.add(v.toString());
                    result.put(k.toString(), list);
                }
            });
        }
        return result;
    }
}
