package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T23:33:13+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class SourceConfigurationMapperImpl implements SourceConfigurationMapper {

    @Override
    public SourceConfigurationEntity toEntity(SourceConfiguration domain) {
        if ( domain == null ) {
            return null;
        }

        SourceConfigurationEntity sourceConfigurationEntity = new SourceConfigurationEntity();

        sourceConfigurationEntity.setId( domain.getId() );
        sourceConfigurationEntity.setCreatedAt( domain.getCreatedAt() );
        sourceConfigurationEntity.setUpdatedAt( domain.getUpdatedAt() );
        sourceConfigurationEntity.setName( domain.getName() );
        sourceConfigurationEntity.setUrl( domain.getUrl() );
        sourceConfigurationEntity.setMethod( domain.getMethod() );
        sourceConfigurationEntity.setAuthType( domain.getAuthType() );
        Map<String, Object> map = domain.getAuthConfig();
        if ( map != null ) {
            sourceConfigurationEntity.setAuthConfig( new LinkedHashMap<String, Object>( map ) );
        }
        sourceConfigurationEntity.setPaginationType( domain.getPaginationType() );
        Map<String, Object> map1 = domain.getPaginationConfig();
        if ( map1 != null ) {
            sourceConfigurationEntity.setPaginationConfig( new LinkedHashMap<String, Object>( map1 ) );
        }
        Map<String, Object> map2 = domain.getRequestOptions();
        if ( map2 != null ) {
            sourceConfigurationEntity.setRequestOptions( new LinkedHashMap<String, Object>( map2 ) );
        }
        sourceConfigurationEntity.setActive( domain.isActive() );

        return sourceConfigurationEntity;
    }

    @Override
    public SourceConfiguration toDomain(SourceConfigurationEntity entity) {
        if ( entity == null ) {
            return null;
        }

        SourceConfiguration.SourceConfigurationBuilder sourceConfiguration = SourceConfiguration.builder();

        sourceConfiguration.id( entity.getId() );
        sourceConfiguration.name( entity.getName() );
        sourceConfiguration.url( entity.getUrl() );
        sourceConfiguration.method( entity.getMethod() );
        sourceConfiguration.authType( entity.getAuthType() );
        Map<String, Object> map = entity.getAuthConfig();
        if ( map != null ) {
            sourceConfiguration.authConfig( new LinkedHashMap<String, Object>( map ) );
        }
        sourceConfiguration.paginationType( entity.getPaginationType() );
        Map<String, Object> map1 = entity.getPaginationConfig();
        if ( map1 != null ) {
            sourceConfiguration.paginationConfig( new LinkedHashMap<String, Object>( map1 ) );
        }
        Map<String, Object> map2 = entity.getRequestOptions();
        if ( map2 != null ) {
            sourceConfiguration.requestOptions( new LinkedHashMap<String, Object>( map2 ) );
        }
        sourceConfiguration.active( entity.isActive() );
        sourceConfiguration.createdAt( entity.getCreatedAt() );
        sourceConfiguration.updatedAt( entity.getUpdatedAt() );

        return sourceConfiguration.build();
    }
}
