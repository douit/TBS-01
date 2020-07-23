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
public class STCPayDirectPaymentAuthorizeRespDTO {

    @JsonProperty("DirectPaymentAuthorizeV4ResponseMessage")
    private DirectPaymentAuthorizeV4ResponseMessage directPaymentAuthorizeV4ResponseMessage;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectPaymentAuthorizeV4ResponseMessage {
        @JsonProperty("OtpReference")
        private String otpReference;
        @JsonProperty("STCPayPmtReference")
        private String sTCPayPmtReference;
        @JsonProperty("ExpiryDuration")
        private Integer expiryDuration;

    }
}
