package sa.tamkeentech.tbs.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link sa.tamkeentech.tbs.domain.Payment} entity.
 */
@ApiModel(description = "Apple pay authorize/purshase request DTO.")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApplePayTokenAuthorizeDTO implements Serializable {

    private PaymentData paymentData ;
    private PaymentMethod paymentMethod;
    private String transactionIdentifier;
    // pass billing transactionId
    // can we use same provided by apple?
    private String transactionIdBilling;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentData {
        private String version;
        private String data;
        private String signature;
        public Header header;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Header {
        private String ephemeralPublicKey;
        private String publicKeyHash;
        private String transactionId;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentMethod {
        private String displayName;
        private String network;
        private String type;
    }

}

