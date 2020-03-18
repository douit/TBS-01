package sa.tamkeentech.tbs.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.domain.Item;
import sa.tamkeentech.tbs.domain.ItemHistory;
import sa.tamkeentech.tbs.service.dto.ItemHistoryDTO;

/**
 * Mapper for the entity {@link ItemHistory} and its DTO {@link ItemHistoryDTO}.
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ClientMapper.class, TaxMapper.class})
public interface ItemHistoryMapper extends EntityMapper<ItemHistoryDTO, ItemHistory> {

    ItemHistoryDTO toDto(ItemHistory item);

    ItemHistory toEntity(ItemHistoryDTO itemDTO);

    @Mapping(source = "id", target = "itemId")
    @Mapping(target = "id", ignore = true)
    ItemHistory itemToEntity(Item item);

    DataTablesOutput<ItemHistoryDTO> toDto(DataTablesOutput<ItemHistory> all);
}
