package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Payment} and its DTO {@link PaymentDTO}.
 */
@Mapper(componentModel = "spring", uses = {InvoiceMapper.class, PaymentMethodMapper.class})
public interface PaymentMapper extends EntityMapper<PaymentDTO, Payment> {

    @Mapping(source = "invoice.id", target = "invoiceId")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    PaymentDTO toDto(Payment payment);

    @Mapping(source = "invoiceId", target = "invoice")
    @Mapping(source = "paymentMethodId", target = "paymentMethod")
    Payment toEntity(PaymentDTO paymentDTO);

    default Payment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setId(id);
        return payment;
    }
}
