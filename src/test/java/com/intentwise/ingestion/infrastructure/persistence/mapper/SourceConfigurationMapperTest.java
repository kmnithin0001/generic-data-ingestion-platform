package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.AuthenticationType;
import com.intentwise.ingestion.domain.model.HttpMethodType;
import com.intentwise.ingestion.domain.model.PaginationType;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for SourceConfigurationMapper mapping rules.
 */
class SourceConfigurationMapperTest {

    private final SourceConfigurationMapper mapper = Mappers.getMapper(SourceConfigurationMapper.class);

    @Test
    void shouldMapSourceConfigurationToEntityAndBack() {
        SourceConfiguration domain = SourceConfiguration.builder()
                .id(UUID.randomUUID())
                .name("Test REST Source")
                .url("https://api.example.com/data")
                .method(HttpMethodType.GET)
                .authType(AuthenticationType.API_KEY)
                .authConfig(Map.of("headerName", "X-API-KEY", "keyValue", "secret-token"))
                .paginationType(PaginationType.CURSOR)
                .paginationConfig(Map.of("cursorParam", "cursor", "limitParam", "size"))
                .requestOptions(Map.of("timeout", 5000))
                .active(true)
                .build();

        SourceConfigurationEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getName(), entity.getName());
        assertEquals(domain.getUrl(), entity.getUrl());
        assertEquals(domain.getMethod(), entity.getMethod());
        assertEquals(domain.getAuthType(), entity.getAuthType());
        assertEquals(domain.getAuthConfig(), entity.getAuthConfig());
        assertEquals(domain.getPaginationType(), entity.getPaginationType());
        assertEquals(domain.getPaginationConfig(), entity.getPaginationConfig());
        assertEquals(domain.getRequestOptions(), entity.getRequestOptions());
        assertTrue(entity.isActive());

        SourceConfiguration mappedBack = mapper.toDomain(entity);

        assertNotNull(mappedBack);
        assertEquals(domain.getId(), mappedBack.getId());
        assertEquals(domain.getName(), mappedBack.getName());
        assertEquals(domain.getUrl(), mappedBack.getUrl());
        assertEquals(domain.getAuthConfig(), mappedBack.getAuthConfig());
        assertEquals(domain.getPaginationConfig(), mappedBack.getPaginationConfig());
        assertTrue(mappedBack.isActive());
    }
}
