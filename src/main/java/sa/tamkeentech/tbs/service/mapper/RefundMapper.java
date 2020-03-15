package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.RefundDTO;

import org.mapstruct.*;
import sa.tamkeentech.tbs.service.dto.RefundDetailedDTO;
import sa.tamkeentech.tbs.service.util.CommonUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for the entity {@link Refund} and its DTO {@link RefundDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RefundMapper extends EntityMapper<RefundDTO, Refund> {


    @Mapping(source = "refund.payment.invoice.accountId", target = "accountId")
    @Mapping(source = "refund.payment.invoice.customer.identity", target = "customerId")
    @Mapping(source = "refund.refundValue", target = "amount")
    RefundDTO toDto(Refund refund);

    @Mapping(source = "refund.payment.invoice.accountId", target = "accountId")
    @Mapping(source = "refund.payment.invoice.customer.identity", target = "customerId")
    @Mapping(source = "refund.payment.invoice.client.name", target = "clientName")
    @Mapping(source = "refund.payment.paymentMethod", target = "paymentMethod")
    @Mapping(source = "refund.refundValue", target = "amount")
    @Mapping(source = "lastModifiedDate", target = "formattedModifiedDate", qualifiedByName = "modifiedDateToDto")
    @Mapping(source = "refund.payment.bankId", target = "bankId")
    RefundDetailedDTO toDetailedDto(Refund refund);

    @Named("modifiedDateToDto")
    default String formatModifiedDate(ZonedDateTime modifiedDate) {
        return CommonUtils.getFormattedLocalDate(modifiedDate, Constants.RIYADH_OFFSET);
    }

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
