package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.PaymentMethodDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link PaymentMethod} and its DTO {@link PaymentMethodDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PaymentMethodMapper extends EntityMapper<PaymentMethodDTO, PaymentMethod> {



    default PaymentMethod fromId(Long id) {
        if (id == null) {
            return null;
        }
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(id);
        return paymentMethod;
    }
}
