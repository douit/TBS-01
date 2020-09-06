package sa.tamkeentech.tbs.service.dto;
import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.*;
import sa.tamkeentech.tbs.domain.enumeration.IdentityType;

import javax.validation.constraints.Email;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Customer} entity.
 */
@ApiModel(description = "The Custom entity.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(of = {"id"})
public class CustomerDTO implements Serializable {

    private Long id;

    private String identity;

    private IdentityType identityType;

    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;

}
