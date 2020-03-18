package sa.tamkeentech.tbs.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.domain.ItemHistory;

import java.util.List;


/**
 * Spring Data  repository for the ItemHistory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemHistoryRepository extends JpaRepository<ItemHistory, Long>, DataTablesRepository<ItemHistory, Long> {

    List<ItemHistory> findByItemId(Long itemId);

}
