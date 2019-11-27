package sa.tamkeentech.tbs.service.dto;

import lombok.*;
import org.checkerframework.checker.units.qual.C;

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
public class ItemDTO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer defaultQuantity;

    private CategoryDTO category;

    private ClientDTO client;

    private Set<TaxDTO> taxes;
}
