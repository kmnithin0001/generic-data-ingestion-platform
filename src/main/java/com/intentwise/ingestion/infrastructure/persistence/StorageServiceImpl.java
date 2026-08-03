package com.intentwise.ingestion.infrastructure.persistence;

import com.intentwise.ingestion.domain.model.IngestionJob;
import com.intentwise.ingestion.domain.model.JobFilter;
import com.intentwise.ingestion.domain.model.PageResult;
import com.intentwise.ingestion.domain.model.RawApiResponse;
import com.intentwise.ingestion.domain.model.SourceConfiguration;
import com.intentwise.ingestion.domain.repository.StorageService;
import com.intentwise.ingestion.infrastructure.persistence.entity.IngestionJobEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.RawApiResponseEntity;
import com.intentwise.ingestion.infrastructure.persistence.entity.SourceConfigurationEntity;
import com.intentwise.ingestion.infrastructure.persistence.mapper.IngestionJobMapper;
import com.intentwise.ingestion.infrastructure.persistence.mapper.RawApiResponseMapper;
import com.intentwise.ingestion.infrastructure.persistence.mapper.SourceConfigurationMapper;
import com.intentwise.ingestion.infrastructure.persistence.repository.IngestionJobRepository;
import com.intentwise.ingestion.infrastructure.persistence.repository.RawApiResponseRepository;
import com.intentwise.ingestion.infrastructure.persistence.repository.SourceConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the StorageService interface.
 * Connects the Domain layer persistence requests to Spring Data JPA repositories
 * by mapping items via MapStruct, maintaining strict architectural separation.
 * Assumes no business validation rules in this persistence mapper.
 */
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final SourceConfigurationRepository sourceConfigRepository;
    private final IngestionJobRepository jobRepository;
    private final RawApiResponseRepository rawResponseRepository;

    private final SourceConfigurationMapper sourceMapper;
    private final IngestionJobMapper jobMapper;
    private final RawApiResponseMapper responseMapper;

    @Override
    @Transactional
    public SourceConfiguration saveSource(SourceConfiguration source) {
        SourceConfigurationEntity entity = sourceMapper.toEntity(source);
        SourceConfigurationEntity saved = sourceConfigRepository.save(entity);
        return sourceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SourceConfiguration> findSourceById(UUID id) {
        return sourceConfigRepository.findById(id)
                .map(sourceMapper::toDomain);
    }

    @Override
    @Transactional
    public IngestionJob saveJob(IngestionJob job) {
        IngestionJobEntity entity = jobMapper.toEntity(job);
        IngestionJobEntity saved = jobRepository.save(entity);
        return jobMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IngestionJob> findJobById(UUID id) {
        return jobRepository.findById(id)
                .map(jobMapper::toDomain);
    }

    @Override
    @Transactional
    public void saveRawResponses(List<RawApiResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        List<RawApiResponseEntity> entities = responses.stream()
                .map(responseMapper::toEntity)
                .collect(Collectors.toList());
        rawResponseRepository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<IngestionJob> findJobs(JobFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<IngestionJobEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (filter.status() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.status()));
                }
                if (filter.sourceId() != null) {
                    predicates.add(cb.equal(root.get("source").get("id"), filter.sourceId()));
                }
                if (filter.createdAfter() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.createdAfter()));
                }
                if (filter.createdBefore() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.createdBefore()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<IngestionJobEntity> entityPage = jobRepository.findAll(spec, pageable);
        List<IngestionJob> domainJobs = entityPage.getContent().stream()
                .map(jobMapper::toDomain)
                .collect(Collectors.toList());

        return new PageResult<>(
                domainJobs,
                entityPage.getTotalElements(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalPages(),
                entityPage.hasNext(),
                entityPage.hasPrevious()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RawApiResponse> findRawResponsesByJobId(UUID jobId) {
        return rawResponseRepository.findByJobId(jobId).stream()
                .map(responseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IngestionJob> findJobByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return Optional.empty();
        }
        return jobRepository.findByIdempotencyKey(idempotencyKey)
                .map(jobMapper::toDomain);
    }
}
