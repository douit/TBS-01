package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.Bank;
import sa.tamkeentech.tbs.domain.Tax;

import java.util.Optional;


/**
 * Spring Data  repository for the Tax entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findById(Long id);

}
