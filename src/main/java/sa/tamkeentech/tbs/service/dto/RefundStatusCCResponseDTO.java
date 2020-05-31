package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;

import java.io.Serializable;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
 */
@ApiModel(description = "Online Payment Response DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RefundStatusCCResponseDTO implements Serializable {

    @JsonProperty("Code")
    private Long code;

    @JsonProperty("Status")
    private RequestStatus status;

    @JsonProperty("TransactionNumber")
    private String refundId;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("RefundLogInfo")
    private PaymentInternalInfo paymentInternalInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor

    public static class PaymentInternalInfo {
        @JsonProperty("StatusCode")
        private Integer code;

        @JsonProperty("Description")
        private String description;

        @JsonProperty("TransactionId")
        private String transactionId;
    }


}
