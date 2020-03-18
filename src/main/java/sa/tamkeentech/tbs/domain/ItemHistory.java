package sa.tamkeentech.tbs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
@Table(name = "item_history")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ItemHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    /*@Id
    @SequenceGenerator(name="seq-gen",sequenceName="item_audit_id_seq")
    @GeneratedValue(strategy= GenerationType.IDENTITY, generator="seq-gen")
    @Column(name="id",unique=true,nullable=false)*/
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @Column(name = "default_quantity")
    private Integer defaultQuantity;

    @Column(name = "total_taxes")
    private BigDecimal totalTaxes;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonIgnoreProperties("items")
    private Category category;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JsonIgnoreProperties("items")
    private Client client;

    @Column(name = "is_flexible_price")
    private boolean flexiblePrice;

    @Column(name = "last_modified_by", nullable = false, length = 50, updatable = false)
    @JsonIgnore
    private String lastModifiedBy;

    @Column(name = "last_modified_date", updatable = false)
    @JsonIgnore
    private ZonedDateTime lastModifiedDate;

}
