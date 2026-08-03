package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * MapStruct mapper for converting between IngestionJob and IngestionJobEntity.
 * Handles lazy loading and partial objects by converting UUID sourceId to a stub Entity.
 */
@Mapper(componentModel = "spring")
public interface IngestionJobMapper {

    /**
     * Converts a domain IngestionJob to IngestionJobEntity.
     * Maps the sourceId UUID using a named stub mapper.
     *
     * @param domain the IngestionJob domain model
     * @return the IngestionJobEntity
     */
    @Mapping(target = "source", source = "sourceId", qualifiedByName = "idToSourceEntity")
    IngestionJobEntity toEntity(IngestionJob domain);

    /**
     * Converts IngestionJobEntity to domain IngestionJob.
     * Extracts sourceId UUID from the referenced SourceConfigurationEntity.
     *
     * @param entity the IngestionJobEntity
     * @return the IngestionJob domain model
     */
    @Mapping(target = "sourceId", source = "source.id")
    IngestionJob toDomain(IngestionJobEntity entity);

    /**
     * Helper mapping method to prevent full entity fetching just to set a foreign key relation.
     * Creates a stub SourceConfigurationEntity with only the ID set.
     *
     * @param sourceId the sourceId UUID
     * @return a SourceConfigurationEntity stub
     */
    @Named("idToSourceEntity")
    default SourceConfigurationEntity idToSourceEntity(UUID sourceId) {
        if (sourceId == null) {
            return null;
        }
        SourceConfigurationEntity entity = new SourceConfigurationEntity();
        entity.setId(sourceId);
        return entity;
    }
}
