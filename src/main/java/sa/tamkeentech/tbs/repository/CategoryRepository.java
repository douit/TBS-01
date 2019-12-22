package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.Category;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data  repository for the Category entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCode(String code);

}
