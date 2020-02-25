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
public class PaymentStatusResponseDTO implements Serializable {

    @JsonProperty("Code")
    private String code;

    @JsonProperty("BillNumber")
    private String billNumber;

    @JsonProperty("TransactionNumber")
    private String transactionId;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("CardNumber")
    private String cardNumber;

    // added for event logging
    private String cardHolderName;
    private String cardExpiryDate;
    private String amount;

    @JsonProperty("PaymentInternalInfo")
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
