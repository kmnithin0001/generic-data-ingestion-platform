package com.intentwise.ingestion.infrastructure.config;

import com.intentwise.ingestion.domain.service.DefaultJobStateMachine;
import com.intentwise.ingestion.domain.service.DefaultProgressCalculator;
import com.intentwise.ingestion.domain.service.JobStateMachine;
import com.intentwise.ingestion.domain.service.ProgressCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class registering domain layer service implementations as beans
 * to maintain clean separation without adding Spring annotations to domain code.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public JobStateMachine jobStateMachine() {
        return new DefaultJobStateMachine();
    }

    @Bean
    public ProgressCalculator progressCalculator() {
        return new DefaultProgressCalculator();
    }
}
