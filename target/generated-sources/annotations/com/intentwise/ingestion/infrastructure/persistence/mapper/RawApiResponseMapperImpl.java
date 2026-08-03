package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.RawApiResponseEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T23:33:13+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class RawApiResponseMapperImpl implements RawApiResponseMapper {

    @Override
    public RawApiResponseEntity toEntity(RawApiResponse domain) {
        if ( domain == null ) {
            return null;
        }

        RawApiResponseEntity rawApiResponseEntity = new RawApiResponseEntity();

        rawApiResponseEntity.setJob( idToJobEntity( domain.getJobId() ) );
        rawApiResponseEntity.setId( domain.getId() );
        rawApiResponseEntity.setCreatedAt( domain.getCreatedAt() );
        rawApiResponseEntity.setUpdatedAt( domain.getUpdatedAt() );
        rawApiResponseEntity.setPageNumber( domain.getPageNumber() );
        rawApiResponseEntity.setRequestUrl( domain.getRequestUrl() );
        Map<String, Object> map = domain.getRequestHeaders();
        if ( map != null ) {
            rawApiResponseEntity.setRequestHeaders( new LinkedHashMap<String, Object>( map ) );
        }
        rawApiResponseEntity.setResponseBody( domain.getResponseBody() );
        Map<String, Object> map1 = domain.getResponseMetadata();
        if ( map1 != null ) {
            rawApiResponseEntity.setResponseMetadata( new LinkedHashMap<String, Object>( map1 ) );
        }

        return rawApiResponseEntity;
    }

    @Override
    public RawApiResponse toDomain(RawApiResponseEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RawApiResponse.RawApiResponseBuilder rawApiResponse = RawApiResponse.builder();

        rawApiResponse.jobId( entityJobId( entity ) );
        rawApiResponse.id( entity.getId() );
        rawApiResponse.pageNumber( entity.getPageNumber() );
        rawApiResponse.requestUrl( entity.getRequestUrl() );
        Map<String, Object> map = entity.getRequestHeaders();
        if ( map != null ) {
            rawApiResponse.requestHeaders( new LinkedHashMap<String, Object>( map ) );
        }
        rawApiResponse.responseBody( entity.getResponseBody() );
        Map<String, Object> map1 = entity.getResponseMetadata();
        if ( map1 != null ) {
            rawApiResponse.responseMetadata( new LinkedHashMap<String, Object>( map1 ) );
        }
        rawApiResponse.createdAt( entity.getCreatedAt() );
        rawApiResponse.updatedAt( entity.getUpdatedAt() );

        return rawApiResponse.build();
    }

    private UUID entityJobId(RawApiResponseEntity rawApiResponseEntity) {
        if ( rawApiResponseEntity == null ) {
            return null;
        }
        IngestionJobEntity job = rawApiResponseEntity.getJob();
        if ( job == null ) {
            return null;
        }
        UUID id = job.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
