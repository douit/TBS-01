package sa.tamkeentech.tbs.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Invoice;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sun.awt.image.MultiResolutionCachedImage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the Invoice entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, DataTablesRepository<Invoice, Long> {
    Optional<Invoice> findByNumber(Long id);
    Optional<Invoice> findById(Long id);

    @Query(value = "SELECT date_trunc('day', invoice.created_date) As Day , count(*) As totalInvoice , \n" +
        "        sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice"+
        " FROM invoice WHERE invoice.created_date >=?1" +
        "        AND invoice.created_date <=?2" +
        "        group by Day ORDER BY Day;", nativeQuery = true)
    List<Object[]> getStatisticsByMonth(ZonedDateTime from, ZonedDateTime to);

    @Query(value = "SELECT count(*) " +
        "FROM invoice WHERE payment_status = 'PAID' ", nativeQuery = true)
    public Long sumPaidInvoice();
    @Query(value = "SELECT sum(grand_total) " +
        "FROM invoice WHERE payment_status = 'PAID' ", nativeQuery = true)
    public BigDecimal sumIncome();

    @Query(value = "SELECT date_trunc('month', invoice.created_date) As Month, count(*) As totalInvoice ,\n" +
        "sum(case when payment_status = 'PAID' then 1 else 0 end) As totalPaid" +
        " FROM invoice WHERE invoice.created_date >= ?1 " +
        "    AND invoice.created_date <= ?2 " +
        "group by Month ORDER BY Month;", nativeQuery = true)
    List<Object[]> getStatisticsByYear(ZonedDateTime from, ZonedDateTime to);

    Page<Invoice> findByPaymentStatusOrderByIdDesc(PaymentStatus status, Pageable pageable);

}
