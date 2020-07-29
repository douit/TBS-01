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
public class STCPayPaymentRefundRespDTO {


    @JsonProperty("RefundPaymentResponseMessage")
    private RefundPaymentResponseMessage refundPaymentResponseMessage;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RefundPaymentResponseMessage {
        @JsonProperty("NewSTCPayRefNum")
        private String newSTCPayRefNum;


    }
}
