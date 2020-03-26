package sa.tamkeentech.tbs.service.dto;
import java.time.Instant;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

import javax.validation.constraints.NotBlank;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
 */
@ApiModel(description = "Online Payment DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentDTO implements Serializable {

    private Long id;

    private PaymentStatus status;

    @NotBlank
    private Long invoiceId;

    private InvoiceDTO invoice;

    private String redirectUrl;

    private String transactionId;

    private PaymentMethodDTO paymentMethod;

    private BigDecimal amount;

    private ZonedDateTime lastModifiedDate;

    // added for notif
    private String billNumber;
    private ZonedDateTime paymentDate;

    // reporting
    private String formattedModifiedDate;

    private String bankId;

}
