package sa.tamkeentech.tbs.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;

/**
 * Mapper for the entity {@link Invoice} and its DTO {@link InvoiceDTO}.
 */
@Mapper(componentModel = "spring", uses = {DiscountMapper.class, CustomerMapper.class, ClientMapper.class, PaymentMapper.class, InvoiceItemMapper.class})
public interface InvoiceMapper extends BasePagingMapper<InvoiceDTO, Invoice> {


    // @Mapping(target = "payments", ignore = true)
    InvoiceDTO toDto(Invoice invoice);

    @Mapping(target = "invoiceItems", ignore = true)
    // @Mapping(target = "payments", ignore = true)
    Invoice toEntity(InvoiceDTO invoiceDTO);

    default Invoice fromId(Long id) {
        if (id == null) {
            return null;
        }
        Invoice invoice = new Invoice();
        invoice.setId(id);
        return invoice;
    }

    DataTablesOutput<InvoiceDTO> toDto(DataTablesOutput<Invoice> all);
}
