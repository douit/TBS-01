package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;


/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Client} entity.
 */

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
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

    @JsonIgnore
    private String clientToken;

    @JsonIgnore
    private ZonedDateTime tokenModifiedDate;

    private String notificationUrl;

}
