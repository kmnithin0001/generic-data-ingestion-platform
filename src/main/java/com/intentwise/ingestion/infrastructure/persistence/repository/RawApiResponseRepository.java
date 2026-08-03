package com.intentwise.ingestion.infrastructure.persistence.repository;

import com.intentwise.ingestion.infrastructure.persistence.entity.RawApiResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for RawApiResponseEntity.
 */
@Repository
public interface RawApiResponseRepository extends JpaRepository<RawApiResponseEntity, UUID> {

    /**
     * Finds raw responses persisted for a specific ingestion job ID.
     *
     * @param jobId the UUID of the job
     * @return the list of responses
     */
    List<RawApiResponseEntity> findByJobId(UUID jobId);
}
