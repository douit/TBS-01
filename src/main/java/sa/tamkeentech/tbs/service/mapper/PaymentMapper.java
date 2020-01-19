package sa.tamkeentech.tbs.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.service.util.CommonUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Mapper for the entity {@link Payment} and its DTO {@link PaymentDTO}.
 */
@Mapper(componentModel = "spring", uses = {InvoiceMapper.class, PaymentMethodMapper.class})
public interface PaymentMapper extends EntityMapper<PaymentDTO, Payment> {

    @Mapping(source = "invoice.id", target = "invoiceId")
    @Mapping(source = "lastModifiedDate", target = "formattedModifiedDate", qualifiedByName = "modifiedDateToDto")
    // @Mapping(source = "paymentMethod.code", target = "paymentMethod")
    PaymentDTO toDto(Payment payment);

    @Named("modifiedDateToDto")
    default String formatModifiedDate(ZonedDateTime modifiedDate) {
        return CommonUtils.getFormattedLocalDate(modifiedDate, Constants.RIYADH_OFFSET);
    }

    // stackoverflow cause toString of invoiceItem
    // @Mapping(source = "invoiceId", target = "invoice")
    @Mapping(target = "invoice", ignore = true)
    // @Mapping(source = "paymentMethodId", target = "paymentMethod")
    Payment toEntity(PaymentDTO paymentDTO);

    default Payment fromId(Long id) {
        if (id == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setId(id);
        return payment;
    }

    DataTablesOutput<PaymentDTO> toDto(DataTablesOutput<Payment> all);
}
