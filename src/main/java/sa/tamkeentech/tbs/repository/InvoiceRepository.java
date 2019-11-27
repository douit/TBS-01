package sa.tamkeentech.tbs.repository;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Invoice;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data  repository for the Invoice entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, DataTablesRepository<Invoice, Long> {
    Optional<Invoice> findByNumber(Long id);

    Optional<Invoice> findById(Long id);

}
