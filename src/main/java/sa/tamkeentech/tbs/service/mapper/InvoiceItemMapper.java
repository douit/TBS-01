package sa.tamkeentech.tbs.service.mapper;

import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.service.dto.InvoiceItemDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link InvoiceItem} and its DTO {@link InvoiceItemDTO}.
 */
@Mapper(componentModel = "spring", uses = {InvoiceMapper.class, DiscountMapper.class, ItemMapper.class})
public interface InvoiceItemMapper extends EntityMapper<InvoiceItemDTO, InvoiceItem> {

    // @Mapping(source = "invoice.id", target = "invoiceId")
    // @Mapping(source = "discount.id", target = "discountId")
    @Mapping(source = "item.id", target = "itemId")
    InvoiceItemDTO toDto(InvoiceItem invoiceItem);

    // @Mapping(source = "invoiceId", target = "invoice")
    // @Mapping(source = "discountId", target = "discount")
    @Mapping(source = "itemId", target = "item")
    InvoiceItem toEntity(InvoiceItemDTO invoiceItemDTO);

    default InvoiceItem fromId(Long id) {
        if (id == null) {
            return null;
        }
        InvoiceItem invoiceItem = new InvoiceItem();
        invoiceItem.setId(id);
        return invoiceItem;
    }
}
