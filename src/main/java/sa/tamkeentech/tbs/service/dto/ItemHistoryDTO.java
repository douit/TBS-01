package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.ItemAudit} entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ItemHistoryDTO implements Serializable {

    private Long id;

    private Long itemId;

    private String code;

    private String name;

    private BigDecimal price;

    private Integer defaultQuantity;

    private CategoryDTO category;

    private ClientDTO client;

    private BigDecimal totalTaxes;

    private Integer quantity;

    private boolean flexiblePrice;

    private String lastModifiedBy;;

    private ZonedDateTime lastModifiedDate;

}
