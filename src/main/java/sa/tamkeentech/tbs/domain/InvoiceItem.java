package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A InvoiceItem.
 */
@Entity
@Table(name = "invoice_item")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class InvoiceItem extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator", sequenceName="sequence_generator")*/
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "tax_name")
    private String taxName;

    @Column(name = "tax_rate", precision = 21, scale = 2)
    private BigDecimal taxRate;

    @ManyToOne
    // @JoinColumn(name = "invoice_id", nullable = false )
    @JsonIgnoreProperties("invoiceItems")
    private Invoice invoice;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(unique = true)
    private Discount discount;

    @ManyToOne
    @JsonIgnoreProperties("invoiceItems")
    private Item item;

}
