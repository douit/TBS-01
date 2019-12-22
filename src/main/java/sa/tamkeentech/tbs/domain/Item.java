package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * A Item.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Entity
@Table(name = "item")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Item extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator", sequenceName="sequence_generator")*/
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @Column(name = "default_quantity")
    private Integer defaultQuantity;


    @ManyToMany(fetch = FetchType.EAGER,  cascade = CascadeType.ALL)
    @JoinTable(name = "item_tax",
        joinColumns = { @JoinColumn(name = "item_id") },
        inverseJoinColumns = { @JoinColumn(name = "tax_id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Tax> taxes = new HashSet<>();

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonIgnoreProperties("items")
    private Category category;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonIgnoreProperties("items")
    private Client client;
}
