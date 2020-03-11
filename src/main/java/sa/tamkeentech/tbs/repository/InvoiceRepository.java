package sa.tamkeentech.tbs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.service.dto.ClientDTO;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the Invoice entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, DataTablesRepository<Invoice, Long>
{
    Optional<Invoice> findByNumber(Long id);
    Optional<Invoice> findById(Long id);
    Optional<Invoice> findByAccountId(Long accountId);
    List<Optional<Invoice>>  findByStatusAndClient(InvoiceStatus invoiceStatus , Client client );

    @Query(value = "SELECT date_trunc('day', invoice.created_date) As Day , count(*) As totalInvoice , \n" +
        "        sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice"+
        " FROM invoice WHERE invoice.created_date >=?1" +
        "        AND invoice.created_date <=?2" +
        "        AND invoice.client_id = ?3 "+
        "        group by Day ORDER BY Day;", nativeQuery = true)
    List<Object[]> getStatisticsByMonth(ZonedDateTime from, ZonedDateTime to , long clientId);

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
        "        AND invoice.client_id = ?3 "+
        "group by Month ORDER BY Month;", nativeQuery = true)
    List<Object[]> getStatisticsByYear(ZonedDateTime from, ZonedDateTime to, long clientId);

    @Query(value = "SELECT * " +
        " FROM invoice WHERE invoice.expiry_date <= ?1 " +
        "    AND invoice.payment_status in ('UNPAID', 'PENDING')  \n"+
        "AND invoice.status != 'EXPIRED' \n"+
        "AND invoice.status != 'FAILED'", nativeQuery = true)
    List<Invoice> getExpiryInvoices(ZonedDateTime currentDate);

    Page<Invoice> findByPaymentStatusOrderByIdDesc(PaymentStatus status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update Invoice i set i.status = ?2 where i.id = ?1")
    int setStatus(Long id, InvoiceStatus status);

    @Query(value = "SELECT date_trunc('month', invoice.created_date) As Month, count(*) As totalInvoice ,\n" +
        "sum(case when payment_status = 'PAID' then 1 else 0 end) As totalPaid" +
        " FROM invoice WHERE invoice.created_date >= ?1 " +
        "    AND invoice.created_date <= ?2 " +
        "        AND invoice.client_id = ?3 "+
        "group by Month ORDER BY Month;", nativeQuery = true)
    List<Object[]> getExpiredInvoices(ZonedDateTime from, ZonedDateTime to, long clientId);

    List<Invoice> findTop1000ByCustomerIdentity(String customerId);
}
