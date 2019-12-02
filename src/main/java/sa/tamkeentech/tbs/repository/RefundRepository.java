package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.Refund;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

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
}
