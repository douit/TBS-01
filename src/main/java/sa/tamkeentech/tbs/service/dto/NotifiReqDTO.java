package sa.tamkeentech.tbs.service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "NotifiReqDTO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotifiReqDTO {

    private  String billId;
    private String billAccount;
    private String amount;
    private String paymentDate;
    private String bankId;
    private String transactionPaymentId;

}
