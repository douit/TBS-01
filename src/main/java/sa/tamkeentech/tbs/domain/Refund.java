package sa.tamkeentech.tbs.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

/**
 * A Refund.
 */
@Entity
@Table(name = "refund")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Refund extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "refund_id")
    private String refundId;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Refund amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Refund status(PaymentStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getRefundId() {
        return refundId;
    }

    public Refund refundId(String refundId) {
        this.refundId = refundId;
        return this;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Refund)) {
            return false;
        }
        return id != null && id.equals(((Refund) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Refund{" +
            "id=" + getId() +
            ", amount=" + getAmount() +
            ", status='" + getStatus() + "'" +
            ", refundId='" + getRefundId() + "'" +
            "}";
    }
}
