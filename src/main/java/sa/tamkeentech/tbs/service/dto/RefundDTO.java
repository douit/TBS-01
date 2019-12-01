package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Refund} entity.
 */
@ApiModel(description = "Refund DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RefundDTO implements Serializable {

    private Long id;
    private BigDecimal amount;
    private PaymentStatus status;
    // private String refundId;
    // private String billerId;
    // private String bankId;
    private String officialId;
    private int paymetTransactionId;
    private int applicationId;
    private String customerId;
    // private String customerIdType;
}
