package io.github.zaraporsche911cloud.reportingassistant.audit;

import io.github.zaraporsche911cloud.reportingassistant.dto.common.PageResponse;
import io.github.zaraporsche911cloud.reportingassistant.entity.AuditLog;
import io.github.zaraporsche911cloud.reportingassistant.repository.AuditLogRepository;
import io.github.zaraporsche911cloud.reportingassistant.service.PageRequestFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final PageRequestFactory pageRequestFactory;

    public AuditService(AuditLogRepository repository, PageRequestFactory pageRequestFactory) {
        this.repository = repository;
        this.pageRequestFactory = pageRequestFactory;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String actor, String action, String resourceType, Object resourceId, String detail) {
        repository.save(new AuditLog(actor, action, resourceType,
                resourceId == null ? null : String.valueOf(resourceId), detail, MDC.get("correlationId")));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> list(String action, int page, int size) {
        var pageable = pageRequestFactory.create(page, size, "createdAt", Sort.Direction.DESC, Set.of("createdAt"));
        Page<AuditLog> logs = action == null || action.isBlank()
                ? repository.findAll(pageable)
                : repository.findByActionContainingIgnoreCase(action, pageable);
        return PageResponse.from(logs.map(log -> new AuditResponse(
                log.getId(), log.getActorEmail(), log.getAction(), log.getResourceType(), log.getResourceId(),
                log.getDetail(), log.getCorrelationId(), log.getCreatedAt())));
    }

    public record AuditResponse(
            Long id, String actorEmail, String action, String resourceType, String resourceId,
            String detail, String correlationId, java.time.Instant createdAt
    ) {
    }
}
