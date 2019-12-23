package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;



/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Client} entity.
 */

@Data
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ClientDTO implements Serializable {

    private Long id;

    private String clientId;

    // private String clientSecret;

    private String name;

    private String logo;

    // private DateUnit dueDateUnit;

    // private Integer dueDateValue;

    private String vatNumber;

    // private String clientToken;

    // private ZonedDateTime tokenModifiedDate;

}
