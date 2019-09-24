package sa.tamkeentech.tbs.repository;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data  repository for the PaymentMethod entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByCode(String code);
}
