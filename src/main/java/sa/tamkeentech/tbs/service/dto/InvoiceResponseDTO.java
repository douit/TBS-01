package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Many items invoice.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InvoiceResponseDTO {

    private static final long serialVersionUID = 1L;
    private int statusId;
    private  String shortDesc;
    private String description;
    private String billNumber;
    private String link ;
    private Long paymentMethod ;

    // extra prams to avoid get invoice after create
    private String vatNumber;
    private Integer billerId;
    private String companyName;
}
