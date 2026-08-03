package com.intentwise.ingestion.presentation.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that populates Correlation ID and Request ID into SLF4J MDC context
 * for structured request tracking. Propagates them back into client headers.
 */
@Component
public class MdcFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    private static final String CORRELATION_ID_MDC = "correlationId";
    private static final String REQUEST_ID_MDC = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.trim().isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // Set MDC
            MDC.put(CORRELATION_ID_MDC, correlationId);
            MDC.put(REQUEST_ID_MDC, requestId);

            // Propagate in response
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

            try {
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
