package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;

import java.io.Serializable;

@ApiModel(description = "Online Payment Response DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RefundResponseDTO implements Serializable {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("URL")
    private String url;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("BillerId")
    private String billerId;

    @JsonProperty("RefundRec")
    private RefundRec refundRec;

    @JsonProperty("Status")
    private Status status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Status{
        @JsonProperty("StatusCode")
        private Integer statusCode;
    }

}



@Data
@AllArgsConstructor
@NoArgsConstructor
class RefundRec{
    @JsonProperty("CustId")
    private CustId custId;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
 class CustId{
    @JsonProperty("OfficialId")
    private String officialId;
    @JsonProperty("OfficialIdType")
    private String officialIdType;

}

