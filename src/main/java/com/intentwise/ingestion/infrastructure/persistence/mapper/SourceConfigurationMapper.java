package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between SourceConfiguration and SourceConfigurationEntity.
 */
@Mapper(componentModel = "spring")
public interface SourceConfigurationMapper {

    /**
     * Converts a domain model to a database entity.
     *
     * @param domain the SourceConfiguration domain model
     * @return the SourceConfigurationEntity
     */
    SourceConfigurationEntity toEntity(SourceConfiguration domain);

    /**
     * Converts a database entity to a domain model.
     *
     * @param entity the SourceConfigurationEntity
     * @return the SourceConfiguration domain model
     */
    SourceConfiguration toDomain(SourceConfigurationEntity entity);
}
