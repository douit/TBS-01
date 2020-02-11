package sa.tamkeentech.tbs.repository;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the Payment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, DataTablesRepository<Payment, Long> {

    Payment findByTransactionId(String transactionId);

    Optional<Payment> findFirstByInvoiceAccountIdAndStatus(Long accountId, PaymentStatus status);

    List<Payment> findByInvoiceAccountIdOrderById(Long accountId);
}
