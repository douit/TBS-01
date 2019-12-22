package sa.tamkeentech.tbs.service.dto;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Tax} entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class TaxDTO implements Serializable {

    private Long id;

    private String name;

    private String code;

    private BigDecimal rate;

    // private Set<ItemDTO> items;

}
