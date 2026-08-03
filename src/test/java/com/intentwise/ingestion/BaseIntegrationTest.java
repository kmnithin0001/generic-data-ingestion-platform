package com.intentwise.ingestion;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base abstract class for integration tests running against a real MySQL instance.
 * Implements the Singleton Container Pattern to share one container lifecycle across
 * all integration test contexts, preventing connection port mismatch errors on Windows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class BaseIntegrationTest {

    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("ingestion_test")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        // Start container once on class load and keep running for JVM lifetime
        mysqlContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
