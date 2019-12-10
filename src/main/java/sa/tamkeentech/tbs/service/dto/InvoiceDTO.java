package sa.tamkeentech.tbs.service.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import lombok.*;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Invoice} entity.
 */
@ApiModel(description = "The InvoiceDTO entity.")
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(of = {"id"})
public class InvoiceDTO implements Serializable {

    private Long id;

    private CustomerDTO customer;

    private InvoiceStatus status;

    private Long number;

    private String note;

    private ZonedDateTime dueDate;

    private ZonedDateTime expiryDate;

    private BigDecimal subtotal;

    private BigDecimal amount;

    private DiscountDTO discount;

    private ZonedDateTime createdDate;

    private PaymentStatus paymentStatus;

    private List<InvoiceItemDTO> invoiceItems;

    private PaymentMethod paymentMethod;

    private String billNumber; //RESP

    public InvoiceDTO() {
    }
}
