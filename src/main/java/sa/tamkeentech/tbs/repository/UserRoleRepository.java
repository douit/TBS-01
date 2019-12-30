package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.UserRole;


/**
 * Spring Data  repository for the UserRole entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
