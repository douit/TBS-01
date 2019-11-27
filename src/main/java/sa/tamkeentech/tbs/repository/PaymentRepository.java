package sa.tamkeentech.tbs.repository;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Payment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Payment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, DataTablesRepository<Payment, Long> {

    Payment findByTransactionId(String transactionId);
}
