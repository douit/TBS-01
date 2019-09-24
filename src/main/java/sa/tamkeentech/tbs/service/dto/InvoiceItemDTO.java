package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.InvoiceItem} entity.
 */
public class InvoiceItemDTO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private BigDecimal amount;

    private Integer quantity;

    private String taxName;

    private BigDecimal taxRate;


    private Long invoiceId;

    private Long discountId;

    private Long itemId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InvoiceItemDTO invoiceItemDTO = (InvoiceItemDTO) o;
        if (invoiceItemDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), invoiceItemDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "InvoiceItemDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", amount=" + getAmount() +
            ", quantity=" + getQuantity() +
            ", taxName='" + getTaxName() + "'" +
            ", taxRate=" + getTaxRate() +
            ", invoice=" + getInvoiceId() +
            ", discount=" + getDiscountId() +
            ", item=" + getItemId() +
            "}";
    }
}
