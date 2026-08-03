package com.intentwise.ingestion.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for Spring WebClient.
 * Sets up custom connection pools, connection timeouts, read/write timeouts, and memory buffer limits.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a customized WebClient.Builder bean.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Connection Pool settings
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ingestion-connection-pool")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .build();

        // Netty HttpClient with timeouts
        HttpClient nettyClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 seconds connect timeout
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))  // 10 seconds read timeout
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))); // 10 seconds write timeout

        // Exchange strategy to support large payloads (e.g. 10MB memory limit)
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(nettyClient))
                .exchangeStrategies(exchangeStrategies);
    }
}
