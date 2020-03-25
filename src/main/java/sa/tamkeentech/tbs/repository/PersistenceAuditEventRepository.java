package sa.tamkeentech.tbs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sa.tamkeentech.tbs.domain.PersistentAuditEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link PersistentAuditEvent} entity.
 */
public interface PersistenceAuditEventRepository extends JpaRepository<PersistentAuditEvent, Long> {

    List<PersistentAuditEvent> findByPrincipal(String principal);

    List<PersistentAuditEvent> findByAuditEventDateAfter(Instant after);

    List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfter(String principal, Instant after);

    List<PersistentAuditEvent> findByPrincipalAndAuditEventDateAfterAndAuditEventType(String principal, Instant after, String type);

    Page<PersistentAuditEvent> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);

    List<PersistentAuditEvent> findByAuditEventDateBeforeAndAuditEventTypeIn(Instant before, List<String> auditEventTypes);
    Optional<PersistentAuditEvent> findFirstByRefIdOrderByIdDesc(long refId);

    Optional<PersistentAuditEvent> findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(long refId, boolean successful, String eventType);

    List<PersistentAuditEvent> findByRefIdOrderById(Long accountId);
}
