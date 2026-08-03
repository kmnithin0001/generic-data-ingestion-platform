package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.infrastructure.persistence.entity.RawApiResponseEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for RawApiResponseMapper mapping rules.
 */
class RawApiResponseMapperTest {

    private final RawApiResponseMapper mapper = Mappers.getMapper(RawApiResponseMapper.class);

    @Test
    void shouldMapRawApiResponseToEntityAndBack() {
        UUID id = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        RawApiResponse domain = RawApiResponse.builder()
                .id(id)
                .jobId(jobId)
                .pageNumber(1)
                .requestUrl("https://api.example.com/data?page=1")
                .requestHeaders(Map.of("Accept", "application/json"))
                .responseBody("{\"data\":[]}")
                .responseMetadata(Map.of("elapsedTimeMs", 234L))
                .build();

        RawApiResponseEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertNotNull(entity.getJob());
        assertEquals(jobId, entity.getJob().getId());
        assertEquals("{\"data\":[]}", entity.getResponseBody());
        assertEquals(Map.of("Accept", "application/json"), entity.getRequestHeaders());
        assertEquals(Map.of("elapsedTimeMs", 234L), entity.getResponseMetadata());

        RawApiResponse mappedBack = mapper.toDomain(entity);

        assertNotNull(mappedBack);
        assertEquals(id, mappedBack.getId());
        assertEquals(jobId, mappedBack.getJobId());
        assertEquals(1, mappedBack.getPageNumber());
        assertEquals("{\"data\":[]}", mappedBack.getResponseBody());
    }
}
