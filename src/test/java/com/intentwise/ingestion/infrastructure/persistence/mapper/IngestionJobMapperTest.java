package com.intentwise.ingestion.infrastructure.persistence.mapper;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobStatus;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for IngestionJobMapper mapping rules.
 */
class IngestionJobMapperTest {

    private final IngestionJobMapper mapper = Mappers.getMapper(IngestionJobMapper.class);

    @Test
    void shouldMapIngestionJobToEntityAndBack() {
        UUID jobId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        IngestionJob domain = IngestionJob.builder()
                .id(jobId)
                .sourceId(sourceId)
                .status(JobStatus.RUNNING)
                .startTime(now)
                .endTime(now.plusMinutes(5))
                .totalRecordsFetched(1250)
                .totalPagesFetched(5)
                .errorMessage(null)
                .build();

        IngestionJobEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(jobId, entity.getId());
        assertNotNull(entity.getSource());
        assertEquals(sourceId, entity.getSource().getId());
        assertEquals(JobStatus.RUNNING, entity.getStatus());
        assertEquals(now, entity.getStartTime());

        IngestionJob mappedBack = mapper.toDomain(entity);

        assertNotNull(mappedBack);
        assertEquals(jobId, mappedBack.getId());
        assertEquals(sourceId, mappedBack.getSourceId());
        assertEquals(JobStatus.RUNNING, mappedBack.getStatus());
        assertEquals(1250, mappedBack.getTotalRecordsFetched());
        assertEquals(5, mappedBack.getTotalPagesFetched());
    }
}
