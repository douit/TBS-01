package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Invoice} and its DTO {@link InvoiceDTO}.
 */
@Mapper(componentModel = "spring", uses = {DiscountMapper.class, CustomerMapper.class, ClientMapper.class})
public interface InvoiceMapper extends EntityMapper<InvoiceDTO, Invoice> {

    @Mapping(source = "discount.id", target = "discountId")
    @Mapping(source = "customer.identity", target = "customerId")
    @Mapping(source = "client.id", target = "clientId")
    InvoiceDTO toDto(Invoice invoice);

    @Mapping(source = "discountId", target = "discount")
    @Mapping(source = "customerId", target = "customer")
    @Mapping(target = "invoiceItems", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(source = "clientId", target = "client")
    Invoice toEntity(InvoiceDTO invoiceDTO);

    default Invoice fromId(Long id) {
        if (id == null) {
            return null;
        }
        Invoice invoice = new Invoice();
        invoice.setId(id);
        return invoice;
    }
}
