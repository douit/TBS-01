package sa.tamkeentech.tbs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, DataTablesRepository<Report, Long> {

	Page<Report> findByRequestUserId(Long userId, Pageable pageable);

}
