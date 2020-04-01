package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.PayFortOperationRequestDTO;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Service
@Transactional
public class PayFortPaymentService {

    private final Logger log = LoggerFactory.getLogger(PayFortPaymentService.class);

    @Inject
    @Lazy
    PaymentRepository paymentRepository;

    @Inject
    @Lazy
    InvoiceRepository invoiceRepository;

    /*@Inject
    PaymentService paymentService;*/

    @Inject
    private RestTemplate restTemplate;

    @Value("${tbs.payment.payfort-sha-type}")
    private Constants.ShaType shaType;
    @Value("${tbs.payment.payfort-sha-request-phrase}")
    private String requestPhrase;
    @Value("${tbs.payment.payfort-sha-response-phrase}")
    private String responsePhrase;
    @Value("${tbs.payment.payfort-access-code}")
    private String accessCode;
    @Value("${tbs.payment.payfort-merchant-identifier}")
    private String merchantIdentifier;
    @Value("${tbs.payment.payfort-language}")
    private String language;
    @Value("${tbs.payment.payfort-url-form}")
    private String urlForm;
    @Value("${tbs.payment.payfort-url-json}")
    private String urlJson;


    public String initPayment(String transactionId, String expiryDate, String cardNumber, String cardSecurityCode,
                              String cardHolderName) throws JSONException, JsonProcessingException {
        log.info("Request to initiate Payment : {}", transactionId);
        PayFortOperationRequestDTO payfortOperationRequest = PayFortOperationRequestDTO.builder()
            .serviceCommand(Constants.PaymentOperation.TOKENIZATION.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(transactionId)
            .language(language)
            .expiryDate(expiryDate)
            .cardNumber(cardNumber)
            .cardSecurityCode(cardSecurityCode)
            .cardHolderName(cardHolderName)
            // .returnUrl("https://www.google.com")
            // .tokenName() The Token received from the Tokenization process. ???
            .build();
        payfortOperationRequest.setSignature(calculatePayfortRequestSignatureForTokenization(payfortOperationRequest));
        String jsonRequest = new ObjectMapper().writeValueAsString(payfortOperationRequest);
        log.info("+++++++++Payfort Tokenization command is ready now and will be sent to the server++++++++++");
        // jsonRequest = jsonRequest.replace("query_command", "service_command");
        log.info(jsonRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache");
        headers.add("Content-Type", "application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequest, headers);
        String result = restTemplate.postForObject(urlJson, requestEntity, String.class);
        log.info("+++++++++Payfort Tokenization command has been sent the server, the result is as follows +++++++++");
        log.info(result);
        JSONObject resultAsJsonObject = new JSONObject(result);
        if (resultAsJsonObject.has("status") && resultAsJsonObject.getString("status").equals(Constants.PayfortResponseStatus.TOKEN_SUCCESS.getStatus())) {
            log.info("Tokenization command has been done successfully");
            boolean isValidSignature = validateTokenizationSignature(resultAsJsonObject);
            log.info("===> Validating Tokenization command payfort signature response, is valid = {}", isValidSignature);
            if (!isValidSignature) {
                throw new TbsRunTimeException("Invalid payfort server sinature found");
            }
            return resultAsJsonObject.getString("token_name");
        } else {
            log.info("Tokenization command failed");
            return null;
        }
    }

    private String calculatePayfortRequestSignatureForTokenization(PayFortOperationRequestDTO payfortOperationRequest) {
        StringBuilder signatureBuilder = new StringBuilder(requestPhrase);
        signatureBuilder
            .append("access_code=").append(accessCode)
            .append("language=").append(language)
            .append("merchant_identifier=").append(merchantIdentifier)
            .append("merchant_reference=").append(payfortOperationRequest.getMerchantReference())
            //.append("return_url=").append(payfortOperationRequest.getReturnUrl())
            .append("service_command=TOKENIZATION");

        signatureBuilder.append(requestPhrase);
        log.info("The tokenization signature builder's value before applying sha encryption : {}", signatureBuilder.toString());
        String signature = getEncryptedSignature(signatureBuilder.toString());
        log.info("The tokenization signature builder's value after applying sha encryption : {}", signature);
        return signature;
    }

    private boolean validateTokenizationSignature(JSONObject resultAsJsonObject) throws JSONException {
        StringBuilder signatureBuilder = new StringBuilder(responsePhrase);
        signatureBuilder
            .append("access_code=" + resultAsJsonObject.getString("access_code"))
            .append("language=" + resultAsJsonObject.getString("language"))
            .append("merchant_identifier=" + resultAsJsonObject.getString("merchant_identifier"))
            .append("response_code=" + resultAsJsonObject.getString("response_code"))
            .append("response_message=" + resultAsJsonObject.getString("response_message"))
            .append("return_url=" + resultAsJsonObject.getString("return_url"))
            .append("service_command=" + resultAsJsonObject.getString("service_command"))
            .append("status=" + resultAsJsonObject.getString("status"))
            .append("token_name=" + resultAsJsonObject.getString("token_name"))
            .append(responsePhrase);
        String signature = getEncryptedSignature(signatureBuilder.toString());

        if (signature.equals(resultAsJsonObject.getString("signature"))) {
            return true;
        } else {
            return false;
        }
    }

    private String getEncryptedSignature(String signature) {
        String encryptedSignature;
        switch (shaType) {
            case SHA_256:
                encryptedSignature = org.apache.commons.codec.digest.DigestUtils.sha256Hex(signature);
                break;
            case SHA_512:
                encryptedSignature = org.apache.commons.codec.digest.DigestUtils.sha512Hex(signature);
                break;
            default:
                encryptedSignature = StringUtils.EMPTY;
        }
        return encryptedSignature;
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println("generatedsecureHash: " + 111);
        System.out.println("-----------------------");
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    public void testTokenization() throws JSONException, JsonProcessingException {
        initPayment(UUID.randomUUID().toString(), "2105", "4005550000000001", "123", "Ahmed Bouzaien");
    }

}
