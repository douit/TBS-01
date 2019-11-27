package sa.tamkeentech.tbs.service.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.PaymentMethod} entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentMethodDTO implements Serializable {

    private Long id;

    private String name;

    @NotNull
    private String code;

}
