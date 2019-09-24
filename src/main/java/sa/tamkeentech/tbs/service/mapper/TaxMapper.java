package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.TaxDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Tax} and its DTO {@link TaxDTO}.
 */
@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface TaxMapper extends EntityMapper<TaxDTO, Tax> {

    @Mapping(source = "item.id", target = "itemId")
    TaxDTO toDto(Tax tax);

    @Mapping(source = "itemId", target = "item")
    Tax toEntity(TaxDTO taxDTO);

    default Tax fromId(Long id) {
        if (id == null) {
            return null;
        }
        Tax tax = new Tax();
        tax.setId(id);
        return tax;
    }
}
