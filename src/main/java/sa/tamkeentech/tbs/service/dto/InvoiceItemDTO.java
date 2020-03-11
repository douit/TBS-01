package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.persistence.Column;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.InvoiceItem} entity.
 */
@ApiModel(description = "The InvoiceItem entity.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(of = {"id"})
public class InvoiceItemDTO implements Serializable {

    private Long id;

    private Long itemId;

    private String itemCode;

    private String name;

    // private String description;

    private BigDecimal amount;

    private Integer quantity;

    // private String taxName;

    private BigDecimal taxRate;

    private DiscountDTO discount;

    @Size(max = 2000)
    private String details;

    @Size(max = 2000)
    private String arguments;

}
