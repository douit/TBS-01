package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.Tax;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;


/**
 * Spring Data  repository for the Tax entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {

    Optional<Tax> findByName(String name);

}
