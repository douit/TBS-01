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

    /*[NotifiReqDTO(
        billId=4105,
        billAccount=7000000105,
        amount=577.50,
        paymentDate=10/31/2019 12:55:24 PM,
        bankId=AAALSARI,
        transactionPaymentId=2317423415), sadad, Sa@2018#2000]*/

    private  String billId;
    private String billAccount;
    private String amount;
    private String paymentDate;
    private String bankId;
    private String transactionPaymentId;

}
