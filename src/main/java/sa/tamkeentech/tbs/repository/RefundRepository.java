package sa.tamkeentech.tbs.repository;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Refund;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;

import java.math.BigDecimal;


/**
 * Spring Data  repository for the Refund entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query(value = "SELECT sum(amount) " +
        "FROM refund WHERE status != 'PAID' ", nativeQuery = true)
    public BigDecimal amountRefund();

    @Modifying
    @Transactional
    @Query("update Refund r set r.status = ?2 where r.refundId = ?1")
    int setStatus(String refundId, RequestStatus status);
}
