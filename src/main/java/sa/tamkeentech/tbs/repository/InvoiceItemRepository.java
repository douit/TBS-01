package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.InvoiceItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the InvoiceItem entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

}
