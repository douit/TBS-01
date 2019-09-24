package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.RefundDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Refund} and its DTO {@link RefundDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RefundMapper extends EntityMapper<RefundDTO, Refund> {



    default Refund fromId(Long id) {
        if (id == null) {
            return null;
        }
        Refund refund = new Refund();
        refund.setId(id);
        return refund;
    }
}
