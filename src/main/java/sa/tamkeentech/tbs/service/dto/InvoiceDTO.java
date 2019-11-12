package sa.tamkeentech.tbs.service.dto;
import io.swagger.annotations.ApiModel;
import java.time.Instant;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import lombok.*;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Invoice} entity.
 */
@ApiModel(description = "The Invoice entity.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class InvoiceDTO implements Serializable {

    private Long id;

    private String customerId;

    private InvoiceStatus status;

    private Long number;

    private String note;

    private Instant dueDate;

    private BigDecimal subtotal;

    private BigDecimal amount;

    private Long discountId;

    private Long clientId;

}
