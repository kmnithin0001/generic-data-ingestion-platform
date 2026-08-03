package com.intentwise.ingestion.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration class enabling Spring's asynchronous processing
 * and defining a dedicated ThreadPoolTaskExecutor for ingestion workflows.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.async.corePoolSize:4}")
    private int corePoolSize;

    @Value("${app.async.maxPoolSize:8}")
    private int maxPoolSize;

    @Value("${app.async.queueCapacity:50}")
    private int queueCapacity;

    @Value("${app.async.threadNamePrefix:ingestion-async-executor-}")
    private String threadNamePrefix;

    @Value("${app.async.rejectionPolicy:CallerRunsPolicy}")
    private String rejectionPolicy;

    /**
     * Declares the thread pool task executor bean named "ingestionTaskExecutor".
     */
    @Bean(name = "ingestionTaskExecutor")
    public AsyncTaskExecutor ingestionTaskExecutor() {
        log.info("Initializing Ingestion ThreadPoolTaskExecutor: corePoolSize={}, maxPoolSize={}, queueCapacity={}, rejectionPolicy={}",
                corePoolSize, maxPoolSize, queueCapacity, rejectionPolicy);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(resolveRejectionPolicy(rejectionPolicy));
        executor.initialize();
        return executor;
    }

    private RejectedExecutionHandler resolveRejectionPolicy(String policyName) {
        if (policyName == null) {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }
        return switch (policyName.trim()) {
            case "AbortPolicy" -> new ThreadPoolExecutor.AbortPolicy();
            case "DiscardPolicy" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DiscardOldestPolicy" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            case "CallerRunsPolicy" -> new ThreadPoolExecutor.CallerRunsPolicy();
            default -> {
                log.warn("Unknown rejection policy '{}'. Defaulting to CallerRunsPolicy.", policyName);
                yield new ThreadPoolExecutor.CallerRunsPolicy();
            }
        };
    }
}
