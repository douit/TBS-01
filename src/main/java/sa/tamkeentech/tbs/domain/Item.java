package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Entity
@Table(name = "item")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Item extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    @Column(name = "default_quantity")
    private Integer defaultQuantity;

    @OneToMany(mappedBy = "item")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Tax> taxes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("items")
    private Category category;

    @ManyToOne
    @JsonIgnoreProperties("items")
    private Client client;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Item name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public Item description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Item price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDefaultQuantity() {
        return defaultQuantity;
    }

    public Item defaultQuantity(Integer defaultQuantity) {
        this.defaultQuantity = defaultQuantity;
        return this;
    }

    public void setDefaultQuantity(Integer defaultQuantity) {
        this.defaultQuantity = defaultQuantity;
    }

    public Set<Tax> getTaxes() {
        return taxes;
    }

    public Item taxes(Set<Tax> taxes) {
        this.taxes = taxes;
        return this;
    }

    public Item addTax(Tax tax) {
        this.taxes.add(tax);
        tax.setItem(this);
        return this;
    }

    public Item removeTax(Tax tax) {
        this.taxes.remove(tax);
        tax.setItem(null);
        return this;
    }

    public void setTaxes(Set<Tax> taxes) {
        this.taxes = taxes;
    }

    public Category getCategory() {
        return category;
    }

    public Item category(Category category) {
        this.category = category;
        return this;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Client getClient() {
        return client;
    }

    public Item client(Client client) {
        this.client = client;
        return this;
    }

    public void setClient(Client client) {
        this.client = client;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        return id != null && id.equals(((Item) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Item{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", price=" + getPrice() +
            ", defaultQuantity=" + getDefaultQuantity() +
            "}";
    }
}
