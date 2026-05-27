package docs.koda.api.audit.service;

import docs.koda.api.audit.domain.AuditLog;
import docs.koda.api.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Async
    public void log(AuditLog.Event event, UUID userId, String ipAddress, String details) {
        repository.save(AuditLog.builder()
                .event(event)
                .userId(userId)
                .ipAddress(ipAddress)
                .details(details)
                .build());
    }
}
