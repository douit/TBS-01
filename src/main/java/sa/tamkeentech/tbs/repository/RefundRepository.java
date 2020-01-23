package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Spring Data  repository for the Refund entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long>, DataTablesRepository<Refund, Long> {

    @Query(value = "SELECT sum(amount) " +
        "FROM refund WHERE status != 'PAID' ", nativeQuery = true)
    public BigDecimal amountRefund();

    @Modifying
    @Transactional
    @Query("update Refund r set r.status = ?2 where r.id = ?1")
    int setStatus(Long refundId, RequestStatus status);
}
