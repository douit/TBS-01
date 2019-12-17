package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.DateUnit;



/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Client} entity.
 */


public class ClientDTO implements Serializable {

    private Long id;

    private String clientId;

    private String clientSecret;

    private String name;

    private String logo;

    private DateUnit dueDateUnit;

    private Integer dueDateValue;

    private String vatNumber;

    private String clientToken;

    private ZonedDateTime tokenModifiedDate;

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public ZonedDateTime getTokenModifiedDate() {
        return tokenModifiedDate;
    }

    public void setTokenModifiedDate(ZonedDateTime tokenModifiedDate) {
        this.tokenModifiedDate = tokenModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public DateUnit getDueDateUnit() {
        return dueDateUnit;
    }

    public void setDueDateUnit(DateUnit dueDateUnit) {
        this.dueDateUnit = dueDateUnit;
    }

    public Integer getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(Integer dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientDTO clientDTO = (ClientDTO) o;
        if (clientDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), clientDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "ClientDTO{" +
            "id=" + getId() +
            ", clientId='" + getClientId() + "'" +
            ", clientSecret='" + getClientSecret() + "'" +
            ", name='" + getName() + "'" +
            ", logo='" + getLogo() + "'" +
            ", dueDateUnit='" + getDueDateUnit() + "'" +
            ", dueDateValue=" + getDueDateValue() +
            ", vatNumber='" + getVatNumber() + "'" +
            "}";
    }
}
