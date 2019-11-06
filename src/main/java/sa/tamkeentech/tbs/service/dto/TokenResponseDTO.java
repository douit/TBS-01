package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "NotifiReqDTO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TokenResponseDTO {

    String access_token;
    int expires_in;
    int refresh_expires_in;
    String refresh_token ;
    String token_type;
    int notBeforePolicy;
    String session_state;
    String scope;

}
