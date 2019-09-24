package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.util.Objects;
import sa.tamkeentech.tbs.domain.enumeration.IdentityType;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Customer} entity.
 */
public class CustomerDTO implements Serializable {

    private Long id;

    private String identity;

    private IdentityType identityType;

    private String name;


    private Long contactId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }

    public void setIdentityType(IdentityType identityType) {
        this.identityType = identityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CustomerDTO customerDTO = (CustomerDTO) o;
        if (customerDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), customerDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
            "id=" + getId() +
            ", identity='" + getIdentity() + "'" +
            ", identityType='" + getIdentityType() + "'" +
            ", name='" + getName() + "'" +
            ", contact=" + getContactId() +
            "}";
    }
}
