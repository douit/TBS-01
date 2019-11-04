package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
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

import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;

/**
 * The Invoice entity.
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

    @Id
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGeneratorInvoice")
    @SequenceGenerator(name = "sequenceGeneratorInvoice")*/
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status;

    @Column(name = "number")
    private Long number;

    @Column(name = "note")
    private String note;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "subtotal", precision = 21, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;

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
