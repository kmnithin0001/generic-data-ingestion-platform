package com.intentwise.ingestion.infrastructure.persistence.repository;

import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for IngestionJobEntity.
 */
@Repository
public interface IngestionJobRepository extends JpaRepository<IngestionJobEntity, UUID>, JpaSpecificationExecutor<IngestionJobEntity> {

    /**
     * Finds an ingestion job entity by its idempotency key.
     *
     * @param idempotencyKey the idempotency key string
     * @return an Optional containing the entity if found, or empty
     */
    Optional<IngestionJobEntity> findByIdempotencyKey(String idempotencyKey);
}
