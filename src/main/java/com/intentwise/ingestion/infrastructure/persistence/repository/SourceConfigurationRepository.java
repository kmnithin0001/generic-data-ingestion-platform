package com.intentwise.ingestion.infrastructure.persistence.repository;

import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SourceConfigurationEntity.
 */
@Repository
public interface SourceConfigurationRepository extends JpaRepository<SourceConfigurationEntity, UUID> {
}
