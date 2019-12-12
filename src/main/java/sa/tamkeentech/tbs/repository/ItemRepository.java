package sa.tamkeentech.tbs.repository;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import sa.tamkeentech.tbs.domain.Item;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sa.tamkeentech.tbs.service.dto.ItemDTO;

import java.util.Optional;


/**
 * Spring Data  repository for the Item entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, DataTablesRepository<Item, Long> {

    Optional<Item> findByNameAndClientId(String itemName, Long id);


}
