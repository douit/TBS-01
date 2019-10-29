package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Invoice} entity.
 */
@ApiModel(description = "One item invoice.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OneItemInvoiceRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    private int statusId;
    private  String shortDesc;
    private String description;
    private String billNumber;
    private String link ;
    private int paymentMethod ;



    /*@NotBlank
    private String customerId;//req

    @NotBlank
    private String customerIdType;//req

    private String customerName;//req

    private String mobile;//req

    private String email;//req

    @NotBlank
    private String itemName;//req

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal price;//req

    @NotNull
    @Min(value= 1)
    private Integer quantity;//req

    @NotNull
    private PaymentMethodDTO paymentMethod; // req name-code? + RESP

    private String statusId; //RESP

    private String shortDesc;//RESP

    private String description;//RESP

    private String billNumber; //RESP

    private String link; //RESP*/

}
