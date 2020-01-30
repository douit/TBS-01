package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class PaymentNotifReqToClientDTO implements Serializable {

    private String billNumber;

    private String paymentDate;

    private String status;

    private PaymentInternalInfo paymentMethod;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentInternalInfo {
        private String id;

        private String name;
    }


}
