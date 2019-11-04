package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class InvoiceStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String billNumber;

    private String vatNumber;

    private BigDecimal vat;

    private BigDecimal price;//req

    private String itemName;//req

    private Integer quantity;//req

    private Integer billerId;

    private String companyName;

    private String issueDate;

}
