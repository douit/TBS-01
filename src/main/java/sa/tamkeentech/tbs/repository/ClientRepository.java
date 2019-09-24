package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.Client;

import java.util.Optional;


/**
 * Spring Data  repository for the Client entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByClientSecret(String secret);

    Optional<Client> findByClientId(String clientId);

}
