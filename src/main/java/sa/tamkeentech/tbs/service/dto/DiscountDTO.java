package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import io.swagger.annotations.ApiModel;
import lombok.*;
import sa.tamkeentech.tbs.domain.enumeration.DiscountType;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Discount} entity.
 */
@ApiModel(description = "The Discount entity.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class DiscountDTO implements Serializable {

    private Long id;

    private Boolean isPercentage;

    private BigDecimal value;

    private DiscountType type;

}
