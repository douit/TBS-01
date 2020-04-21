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
public class PayFortOperationDTO {
    @JsonProperty("service_command")
    private String serviceCommand;

    @JsonProperty("query_command")
    private String queryCommand;

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
    private String rememberMe;
    // this cause an issue with sig as it is not sent
    //private String rememberMe = "NO";

    @JsonProperty("return_url")
    private String returnUrl;


    // PURCHASE
    private String command;

    private Long amount;

    private String currency;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("customer_ip")
    private String customerIp;

    private String eci;

    @JsonProperty("payment_option")
    private String paymentOption;

    @JsonProperty("order_description")
    private String orderDescription;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("merchant_extra")
    private String merchantExtra;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("settlement_reference")
    private String settlementReference;

    // A two-digit numeric value that indicates the status of the transaction.
    // 13	Purchase Failure.
    // 14	Purchase Success.
    // 17	Tokenization failed.
    // 18	Tokenization success.
    // 44	3ds success.
    // 45	3ds failed.
    private String status;

    // Response descrip example: Signature mismatch
    @JsonProperty("response_message")
    private String responseMessage;

    @JsonProperty("response_code")
    private String responseCode;

    @JsonProperty("fort_id")
    private String fortId;

    @JsonProperty("3ds_url")
    private String url3ds;

    ///////Test Mada

    @JsonProperty("authorization_code")
    private String authorizationCode;

}
