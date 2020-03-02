package sa.tamkeentech.tbs.service;

import io.github.jhipster.config.JHipsterProperties;
import sa.tamkeentech.tbs.config.audit.AuditEventConverter;
import sa.tamkeentech.tbs.domain.PersistentAuditEvent;
import sa.tamkeentech.tbs.repository.PersistenceAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator {@code AuditEventRepository}.
 */
@Service
@Transactional
public class AuditEventService {

    private final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final JHipsterProperties jHipsterProperties;

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public AuditEventService(
        PersistenceAuditEventRepository persistenceAuditEventRepository,
        AuditEventConverter auditEventConverter, JHipsterProperties jhipsterProperties) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
        this.jHipsterProperties = jhipsterProperties;
    }

    /**
    * Old audit events should be automatically deleted after 30 days.
    *
    * This is scheduled to get fired at 4:00 (am).
    */
    @Scheduled(cron = "${tbs.cron.audit-event-delete}")
    public void removeOldAuditEvents() {
        persistenceAuditEventRepository
            .findByAuditEventDateBeforeAndAuditEventTypeIn(Instant.now().minus(jHipsterProperties.getAuditEvents().getRetentionPeriod(), ChronoUnit.DAYS),
                Arrays.asList("AUTHENTICATION_SUCCESS", "AUTHENTICATION_FAILURE"))
            .forEach(auditEvent -> {
                log.debug("Deleting audit data {}", auditEvent.toString());
                persistenceAuditEventRepository.delete(auditEvent);
        });
    }

    public Page<AuditEvent> findAll(Pageable pageable) {
        return persistenceAuditEventRepository.findAll(pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }

    public Page<AuditEvent> findByDates(Instant fromDate, Instant toDate, Pageable pageable) {
        return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map(auditEventConverter::convertToAuditEvent);
    }

    public Optional<AuditEvent> find(Long id) {
        return Optional.ofNullable(persistenceAuditEventRepository.findById(id))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(auditEventConverter::convertToAuditEvent);
    }

    public List<PersistentAuditEvent> findInvoiceAudit(Long accountId) {
        return persistenceAuditEventRepository.findByRefId(accountId);
    }
}
