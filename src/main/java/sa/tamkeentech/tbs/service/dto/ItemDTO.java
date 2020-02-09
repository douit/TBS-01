package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.checkerframework.checker.units.qual.C;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Item} entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ItemDTO implements Serializable {

    private Long id;

    private String code;

    private String name;

    private BigDecimal price;

    @Min(value= 1)
    private Integer defaultQuantity;

    private CategoryDTO category;

    private ClientDTO client;

    private Set<TaxDTO> taxes;

    private Integer quantity;

    private boolean flexiblePrice;



}
