package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Refund} entity.
 */
public class RefundDTO implements Serializable {

    private Long id;
    private BigDecimal amount;
    private PaymentStatus status;
    private String refundId;
    private String billerId;
    private String bankId;
    private String officialId;
    private int paymetTransactionId;
    private int applicationId;
    private String customerId;
    private String customerIdType;

    public String getCustomerId() {
        return customerId;
    }

    public String getBillerId() {
        return billerId;
    }

    public void setBillerId(String billerId) {
        this.billerId = billerId;
    }

    public String getOfficialId() {
        return officialId;
    }

    public void setOfficialId(String officialId) {
        this.officialId = officialId;
    }

    public String getCustomerIdType() {
        return customerIdType;
    }

    public void setCustomerIdType(String customerIdType) {
        this.customerIdType = customerIdType;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getPaymetTransactionId() {
        return paymetTransactionId;
    }

    public void setPaymetTransactionId(int paymetTransactionId) {
        this.paymetTransactionId = paymetTransactionId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }


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

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RefundDTO refundDTO = (RefundDTO) o;
        if (refundDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), refundDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RefundDTO{" +
            "id=" + getId() +
            ", amount=" + getAmount() +
            ", status='" + getStatus() + "'" +
            ", refundId='" + getRefundId() + "'" +
            "}";
    }
}
