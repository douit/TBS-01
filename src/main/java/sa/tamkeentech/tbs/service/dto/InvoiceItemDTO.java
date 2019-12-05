package sa.tamkeentech.tbs.service.dto;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.InvoiceItem} entity.
 */
@ApiModel(description = "The InvoiceItem entity.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class InvoiceItemDTO implements Serializable {

    private Long id;

    private Long itemId;

    private String name;

    private String description;

    private BigDecimal amount;

    private Integer quantity;

    private String taxName;

    private BigDecimal taxRate;

    private DiscountDTO discount;

}
