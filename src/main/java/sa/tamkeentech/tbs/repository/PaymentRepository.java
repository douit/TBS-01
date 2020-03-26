package sa.tamkeentech.tbs.repository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the Payment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, DataTablesRepository<Payment, Long> {

    String PAYMENT_BY_TRANSACTION_ID = "paymentByTransactionId";

    @Cacheable(cacheNames = PAYMENT_BY_TRANSACTION_ID)
    Payment findByTransactionId(String transactionId);

    Optional<Payment> findFirstByInvoiceAccountIdAndStatus(Long accountId, PaymentStatus status);

    Optional<Payment> findTopByInvoiceAccountIdAndPaymentMethodCodeOrderByIdDesc(Long accountId, String paymentMethod);

    List<Payment> findByInvoiceAccountIdOrderById(Long accountId);

    List<Payment> findByStatusAndAndLastModifiedDateBetween(PaymentStatus checkoutPageRendered, ZonedDateTime zonedDateTime, ZonedDateTime zonedDateTime1);
}
