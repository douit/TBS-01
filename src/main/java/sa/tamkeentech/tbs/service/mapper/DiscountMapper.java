package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.DiscountDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Discount} and its DTO {@link DiscountDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DiscountMapper extends EntityMapper<DiscountDTO, Discount> {



    default Discount fromId(Long id) {
        if (id == null) {
            return null;
        }
        Discount discount = new Discount();
        discount.setId(id);
        return discount;
    }
}
