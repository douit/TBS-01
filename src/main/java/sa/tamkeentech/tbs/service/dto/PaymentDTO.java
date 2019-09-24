package sa.tamkeentech.tbs.service.dto;
import java.time.Instant;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
 */
public class PaymentDTO implements Serializable {

    private Long id;

    private BigDecimal amount;

    private PaymentStatus status;

    private Instant expirationDate;


    private Long invoiceId;

    private Long paymentMethodId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PaymentDTO paymentDTO = (PaymentDTO) o;
        if (paymentDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), paymentDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
            "id=" + getId() +
            ", amount=" + getAmount() +
            ", status='" + getStatus() + "'" +
            ", expirationDate='" + getExpirationDate() + "'" +
            ", invoice=" + getInvoiceId() +
            ", paymentMethod=" + getPaymentMethodId() +
            "}";
    }
}
