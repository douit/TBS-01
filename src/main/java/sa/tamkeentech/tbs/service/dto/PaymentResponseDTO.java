package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

import javax.validation.constraints.NotBlank;
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
public class PaymentResponseDTO implements Serializable {

    @JsonProperty("Code")
    private Long code;

    @JsonProperty("URL")
    private String url;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TransactionId")
    private String transactionId;

}
