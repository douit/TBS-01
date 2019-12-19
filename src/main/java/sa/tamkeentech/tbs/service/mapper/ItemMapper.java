package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.ItemDTO;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Item} and its DTO {@link ItemDTO}.
 */
@Mapper(componentModel = "spring", uses = {CategoryMapper.class, ClientMapper.class, TaxMapper.class})
public interface ItemMapper extends EntityMapper<ItemDTO, Item> {

    // @Mapping(source = "category.id", target = "categoryId")
    // @Mapping(source = "client.id", target = "clientId")
    ItemDTO toDto(Item item);

    // @Mapping(target = "taxes", ignore = true)
   // @Mapping(target = "removeTax", ignore = true)
    // @Mapping(source = "categoryId", target = "category")
    // @Mapping(source = "clientId", target = "client")
    Item toEntity(ItemDTO itemDTO);

    default Item fromId(Long id) {
        if (id == null) {
            return null;
        }
        Item item = new Item();
        item.setId(id);
        return item;
    }

    DataTablesOutput<ItemDTO> toDto(DataTablesOutput<Item> all);
}
