package sa.tamkeentech.tbs.service.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
 */
@ApiModel(description = "Online Payment Response DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// @JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentNotifResFromClientDTO implements Serializable {

    private Integer statusId;

    private String shortDesc;

    private String description;

}
