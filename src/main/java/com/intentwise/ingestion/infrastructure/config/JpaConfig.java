package com.intentwise.ingestion.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class enabling JPA Auditing separately from the main application class.
 * This prevents JPA bootstrapping issues during controller slice testing.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
