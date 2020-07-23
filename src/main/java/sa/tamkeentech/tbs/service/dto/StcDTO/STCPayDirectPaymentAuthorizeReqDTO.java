package sa.tamkeentech.tbs.service.dto.StcDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@ApiModel(description = "STC pay Direct Payment Authorize request DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class STCPayDirectPaymentAuthorizeReqDTO {

    @JsonProperty("DirectPaymentAuthorizeV4RequestMessage")
    private DirectPaymentAuthorizeV4RequestMessage directPaymentAuthorizeV4RequestMessage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectPaymentAuthorizeV4RequestMessage {
        @JsonProperty("BranchID")
        private String branchID;
        @JsonProperty("TellerID")
        private String tellerID;
        @JsonProperty("DeviceID")
        private String deviceID;
        @JsonProperty("RefNum")
        private String refNum;
        @JsonProperty("BillNumber")
        private String billNumber;
        @JsonProperty("MobileNo")
        private String mobileNo;
        @JsonProperty("Amount")
        private String amount;
        @JsonProperty("MerchantNote")
        private String merchantNote;

    }
}
