package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.RawApiResponseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * MapStruct mapper for converting between RawApiResponse and RawApiResponseEntity.
 * Handles lazy loading and partial objects by converting UUID jobId to a stub Entity.
 */
@Mapper(componentModel = "spring")
public interface RawApiResponseMapper {

    /**
     * Converts a domain RawApiResponse to RawApiResponseEntity.
     * Maps the jobId UUID using a named stub mapper.
     *
     * @param domain the RawApiResponse domain model
     * @return the RawApiResponseEntity
     */
    @Mapping(target = "job", source = "jobId", qualifiedByName = "idToJobEntity")
    RawApiResponseEntity toEntity(RawApiResponse domain);

    /**
     * Converts RawApiResponseEntity to domain RawApiResponse.
     * Extracts jobId UUID from the referenced IngestionJobEntity.
     *
     * @param entity the RawApiResponseEntity
     * @return the RawApiResponse domain model
     */
    @Mapping(target = "jobId", source = "job.id")
    RawApiResponse toDomain(RawApiResponseEntity entity);

    /**
     * Helper mapping method to prevent full entity fetching just to set a foreign key relation.
     * Creates a stub IngestionJobEntity with only the ID set.
     *
     * @param jobId the jobId UUID
     * @return an IngestionJobEntity stub
     */
    @Named("idToJobEntity")
    default IngestionJobEntity idToJobEntity(UUID jobId) {
        if (jobId == null) {
            return null;
        }
        IngestionJobEntity entity = new IngestionJobEntity();
        entity.setId(jobId);
        return entity;
    }
}
