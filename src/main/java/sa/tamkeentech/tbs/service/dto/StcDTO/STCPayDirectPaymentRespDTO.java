package sa.tamkeentech.tbs.service.dto.StcDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "STC pay Direct Payment Authorize response DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class STCPayDirectPaymentRespDTO {

    @JsonProperty("DirectPaymentV4ResponseMessage")
    private DirectPaymentV4ResponseMessage directPaymentV4ResponseMessage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectPaymentV4ResponseMessage {
        @JsonProperty("MerchantID")
        private String merchantID;
        @JsonProperty("BranchID")
        private String branchID;
        @JsonProperty("TellerID")
        private String tellerID;
        @JsonProperty("DeviceID")
        private String deviceID;
        @JsonProperty("RefNum")
        private String refNum;
        @JsonProperty("STCPayRefNum")
        private String stcPayRefNum;
        @JsonProperty("Amount")
        private Integer amount;
        @JsonProperty("PaymentDate")
        private String paymentDate;
        @JsonProperty("PaymentStatus")
        private Integer paymentStatus;
        @JsonProperty("PaymentStatusDesc")
        private String paymentStatusDesc;



    }
}
