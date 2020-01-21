package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.RefundDTO;

import org.mapstruct.*;
import sa.tamkeentech.tbs.service.dto.RefundDetailedDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for the entity {@link Refund} and its DTO {@link RefundDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RefundMapper extends EntityMapper<RefundDTO, Refund> {


    @Mapping(source = "refund.payment.invoice.accountId", target = "accountId")
    @Mapping(source = "refund.payment.invoice.customer.identity", target = "customerId")
    RefundDTO toDto(Refund refund);

    @Mapping(source = "refund.payment.invoice.accountId", target = "accountId")
    @Mapping(source = "refund.payment.invoice.customer.identity", target = "customerId")
    RefundDetailedDTO toDetailedDto(Refund refund);

    default List<RefundDetailedDTO> toDetailedDto(List<Refund> entityList) {
        if ( entityList == null ) {
            return null;
        }
        List<RefundDetailedDTO> list = new ArrayList<RefundDetailedDTO>( entityList.size() );
        for ( Refund refund : entityList ) {
            list.add( toDetailedDto( refund ) );
        }
        return list;
    }

    default Refund fromId(Long id) {
        if (id == null) {
            return null;
        }
        Refund refund = new Refund();
        refund.setId(id);
        return refund;
    }
}
