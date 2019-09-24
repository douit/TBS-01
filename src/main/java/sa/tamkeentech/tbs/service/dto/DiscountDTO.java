package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import sa.tamkeentech.tbs.domain.enumeration.DiscountType;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Discount} entity.
 */
public class DiscountDTO implements Serializable {

    private Long id;

    private Boolean iPercentage;

    private BigDecimal value;

    private DiscountType type;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isiPercentage() {
        return iPercentage;
    }

    public void setiPercentage(Boolean iPercentage) {
        this.iPercentage = iPercentage;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public DiscountType getType() {
        return type;
    }

    public void setType(DiscountType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DiscountDTO discountDTO = (DiscountDTO) o;
        if (discountDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), discountDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DiscountDTO{" +
            "id=" + getId() +
            ", iPercentage='" + isiPercentage() + "'" +
            ", value=" + getValue() +
            ", type='" + getType() + "'" +
            "}";
    }
}
