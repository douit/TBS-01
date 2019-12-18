package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.FileSyncLog;

import java.util.Optional;


/**
 * Spring Data  repository for the FileSyncLog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileSyncLogRepository extends JpaRepository<FileSyncLog, Long> {

    Optional<FileSyncLog> findByFileName(String fileName);

}
