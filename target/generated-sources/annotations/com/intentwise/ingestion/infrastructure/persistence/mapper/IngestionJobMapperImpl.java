package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T23:33:13+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class IngestionJobMapperImpl implements IngestionJobMapper {

    @Override
    public IngestionJobEntity toEntity(IngestionJob domain) {
        if ( domain == null ) {
            return null;
        }

        IngestionJobEntity ingestionJobEntity = new IngestionJobEntity();

        ingestionJobEntity.setSource( idToSourceEntity( domain.getSourceId() ) );
        ingestionJobEntity.setId( domain.getId() );
        ingestionJobEntity.setCreatedAt( domain.getCreatedAt() );
        ingestionJobEntity.setUpdatedAt( domain.getUpdatedAt() );
        ingestionJobEntity.setStatus( domain.getStatus() );
        ingestionJobEntity.setStartTime( domain.getStartTime() );
        ingestionJobEntity.setEndTime( domain.getEndTime() );
        ingestionJobEntity.setTotalRecordsFetched( domain.getTotalRecordsFetched() );
        ingestionJobEntity.setTotalPagesFetched( domain.getTotalPagesFetched() );
        ingestionJobEntity.setErrorMessage( domain.getErrorMessage() );
        ingestionJobEntity.setIdempotencyKey( domain.getIdempotencyKey() );
        ingestionJobEntity.setTotalRecords( domain.getTotalRecords() );
        ingestionJobEntity.setPercentageCompleted( domain.getPercentageCompleted() );
        ingestionJobEntity.setEstimatedCompletion( domain.getEstimatedCompletion() );

        return ingestionJobEntity;
    }

    @Override
    public IngestionJob toDomain(IngestionJobEntity entity) {
        if ( entity == null ) {
            return null;
        }

        IngestionJob.IngestionJobBuilder ingestionJob = IngestionJob.builder();

        ingestionJob.sourceId( entitySourceId( entity ) );
        ingestionJob.id( entity.getId() );
        ingestionJob.status( entity.getStatus() );
        ingestionJob.startTime( entity.getStartTime() );
        ingestionJob.endTime( entity.getEndTime() );
        ingestionJob.totalRecordsFetched( entity.getTotalRecordsFetched() );
        ingestionJob.totalPagesFetched( entity.getTotalPagesFetched() );
        ingestionJob.errorMessage( entity.getErrorMessage() );
        ingestionJob.createdAt( entity.getCreatedAt() );
        ingestionJob.updatedAt( entity.getUpdatedAt() );
        ingestionJob.idempotencyKey( entity.getIdempotencyKey() );
        ingestionJob.totalRecords( entity.getTotalRecords() );
        ingestionJob.percentageCompleted( entity.getPercentageCompleted() );
        ingestionJob.estimatedCompletion( entity.getEstimatedCompletion() );

        return ingestionJob.build();
    }

    private UUID entitySourceId(IngestionJobEntity ingestionJobEntity) {
        if ( ingestionJobEntity == null ) {
            return null;
        }
        SourceConfigurationEntity source = ingestionJobEntity.getSource();
        if ( source == null ) {
            return null;
        }
        UUID id = source.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
