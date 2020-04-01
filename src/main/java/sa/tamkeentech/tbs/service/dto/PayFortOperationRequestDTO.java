package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// @EqualsAndHashCode(of = {"id"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PayFortOperationRequestDTO {
    @JsonProperty("service_command")
    private String serviceCommand;

    @JsonProperty("access_code")
    private String accessCode;

    @JsonProperty("merchant_identifier")
    private String merchantIdentifier;

    @JsonProperty("merchant_reference")
    private String merchantReference;

    private String language;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("card_security_code")
    private String cardSecurityCode;

    private String signature;

    @JsonProperty("token_name")
    private String tokenName;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("remember_me")
    private String rememberMe = "NO";

    @JsonProperty("return_url")
    private String returnUrl;

}
