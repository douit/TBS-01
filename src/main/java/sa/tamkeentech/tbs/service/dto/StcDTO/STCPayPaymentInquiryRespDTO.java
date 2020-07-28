package sa.tamkeentech.tbs.service.dto.StcDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@ApiModel(description = "STC pay Direct Payment Authorize response DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class STCPayPaymentInquiryRespDTO {

    @JsonProperty("DirectPaymentV4ResponseMessage")
    private PaymentInquiryV4ResponseMessage paymentInquiryV4ResponseMessage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentInquiryV4ResponseMessage {
        @JsonProperty("TransactionList")
        private List<TransactionList> transactionList;

    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionList {
        @JsonProperty("MerchantID")
        private String MerchantID;
        @JsonProperty("BranchID")
        private String BranchID;
        @JsonProperty("TellerID")
        private String TellerID;
        @JsonProperty("DeviceID")
        private String DeviceID;
        @JsonProperty("RefNum")
        private String RefNum;
        @JsonProperty("STCPayRefNum")
        private Integer stcPayRefNum;
        @JsonProperty("Amount")
        private Integer Amount;
        @JsonProperty("AmountReversed")
        private Integer amountReversed;
        @JsonProperty("PaymentDate")
        private ZonedDateTime paymentDate;
        @JsonProperty("PaymentStatus")
        private Integer paymentStatus;
        @JsonProperty("PaymentStatusDesc")
        private String paymentStatusDesc;

    }
}
