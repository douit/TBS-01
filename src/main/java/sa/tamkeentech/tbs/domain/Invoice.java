package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Generated;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;

/**
 * The Employee entity.
 */
@Entity
@Table(name = "invoice")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Invoice extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final long DIFF = 6999996000L;

    @Id
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator_invoice")
    @SequenceGenerator(name = "sequence_generator_invoice", sequenceName="sequence_generator_invoice", allocationSize = 1)*/
    // No rollback for sequence so invoice will be created even if Sadad is down
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private InvoiceStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status")
    private InvoiceStatus notificationStatus;

    @Column(name = "bill_id")
    private Long number;

    @Column(name = "note")
    private String note;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "expiry_date")
    private Instant expiryDate;

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

    @OneToMany(mappedBy = "invoice", fetch = FetchType.EAGER, cascade=CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    @OneToMany(mappedBy = "invoice")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Payment> payments = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("invoices")
    private Client client;
}
