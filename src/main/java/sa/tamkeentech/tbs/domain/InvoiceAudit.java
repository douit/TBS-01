package sa.tamkeentech.tbs.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import sa.tamkeentech.tbs.domain.embeddable.InvoiceAuditPrimaryKey;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.NotificationStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Employee entity.
 */
@Entity
@Table(name = "invoice_aud")
@Data
@EqualsAndHashCode
public class InvoiceAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private InvoiceAuditPrimaryKey invoiceAuditPrimaryKey;

    @Column(name = "revtype")
    private int revisionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status")
    private NotificationStatus notificationStatus;

    @Column(name = "bill_id")
    private Long number;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "note")
    private String note;

    @Column(name = "due_date")
    private ZonedDateTime dueDate;

    @Column(name = "expiry_date")
    private ZonedDateTime expiryDate;

    @Column(name = "subtotal", precision = 21, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "grand_total", precision = 21, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_fees", precision = 21, scale = 2)
    private BigDecimal taxFees;

    @OneToOne
    @JoinColumn(unique = true)
    private Discount discount;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JsonIgnoreProperties("invoices")
    private Client client;
}
