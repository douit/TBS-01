package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.Tax;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Tax entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

}
