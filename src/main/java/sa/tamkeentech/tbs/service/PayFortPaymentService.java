package sa.tamkeentech.tbs.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.PayFortOperationDTO;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

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
    @Value("${tbs.payment.payfort-process-payment}")
    private String checkoutPage;


    public PayFortOperationDTO initPayment(Long invoiceNumber) throws UnsupportedEncodingException {
        log.info("Request to initiate Payment : {}", invoiceNumber);
        DateFormat df = new SimpleDateFormat("HHmmss");
        String transactionId = invoiceNumber.toString() + df.format(new Timestamp(System.currentTimeMillis()));

        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String, String> map = new TreeMap();
        map.put("service_command",Constants.PaymentOperation.TOKENIZATION.name());
        map.put("access_code",accessCode);
        map.put("merchant_identifier",merchantIdentifier);
        map.put("merchant_reference",transactionId);
        map.put("language",language);
        map.put("return_url",checkoutPage);

        PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
            .serviceCommand(Constants.PaymentOperation.TOKENIZATION.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(transactionId)
            .language(language)
            .returnUrl(checkoutPage)
            .build();
        payfortOperationRequest.setSignature(calculatePayfortRequestSignatureForTokenization(map));

        return payfortOperationRequest;
    }

    public ResponseEntity<PayFortOperationDTO> processPayment(Map<String, String> params) {
        // PayFort resp:
        // response_code=18000&card_number=400555******0001&card_holder_name=Ahmed+B
        // &signature=07793d77079cc89a769281ed55d3237b71583c76172c6350cfe3ed1f24304621
        // &merchant_identifier=e93bbe3b&expiry_date=2105&access_code=D3KyGokx8hLlQmOVszty
        // &language=en&service_command=TOKENIZATION
        // &response_message=Success&merchant_reference=7000000360021648
        // &token_name=94c729e0864d413687035ac2fe4add31 &return_url=http%3A%2F%2Flocalhost%3A9000%2Fapi%2Fpayments%2Fpayfort-processing
        //&card_bin=400555&status=18
        log.debug("------Payfort payment processing tokenizaion code: {}, message: {}", params.get("status"),  params.get("response_message"));
        PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
            .command(Constants.PaymentOperation.PURCHASE.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(params.get("merchant_reference"))
            .amount(10000l)// 100 SAR
            .currency("SAR")
            .language(language)
            .customerEmail("a.bouzaien.@tamkeentech.sa")
            .customerIp("192.178.1.10") // detect public ip
            .tokenName(params.get("token_name"))
            // .signature() set below
            // .paymentOption("VISA") // pass list? MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            // .eci("ECOMMERCE")
            .orderDescription("Test integration")
            .customerName("Ahmed B")
            .settlementReference(params.get("merchant_reference"))
            .returnUrl("http://localhost:9000/#/customer/test-payfort")
            .build();
        Map<String, String> map = new TreeMap();
        map.put("service_command", Constants.PaymentOperation.PURCHASE.name());
        map.put("access_code", accessCode);
        map.put("merchant_identifier", merchantIdentifier);
        map.put("merchant_reference", payfortOperationRequest.getMerchantReference());
        map.put("amount", payfortOperationRequest.getAmount().toString());
        map.put("currency", payfortOperationRequest.getCurrency());
        map.put("language", language);
        map.put("customer_email", payfortOperationRequest.getCustomerEmail());
        map.put("customer_ip", payfortOperationRequest.getCustomerIp());
        map.put("token_name", payfortOperationRequest.getTokenName());
        // both optional
        /*map.put("payment_option", payfortOperationRequest.getPaymentOption());
        map.put("eci", payfortOperationRequest.getEci());*/
        map.put("order_description", payfortOperationRequest.getOrderDescription());
        map.put("customer_name", payfortOperationRequest.getCustomerName());
        map.put("settlement_reference", payfortOperationRequest.getSettlementReference());
        map.put("return_url", payfortOperationRequest.getReturnUrl());
        payfortOperationRequest.setSignature(calculatePayfortRequestSignatureForTokenization(map));
        log.debug("Purchase request: {}", payfortOperationRequest);
        ResponseEntity<PayFortOperationDTO> result = restTemplate.postForEntity(urlJson, payfortOperationRequest, PayFortOperationDTO.class);
        return result;
    }

    /**
     * Method calculates the signature needed in checkout page
     * @param requestMap
     * @param requestMap
     * @return signature
     */
    private String calculatePayfortRequestSignatureForTokenization(Map<String, String> requestMap) {
        StringBuilder signatureBuilder = new StringBuilder(requestPhrase);
        for(Map.Entry<String, String> entry: requestMap.entrySet()) {
            signatureBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        signatureBuilder.append(requestPhrase);
        log.info("The tokenization of transaction {}, signature builder's value before applying sha encryption : {}", requestMap.get("merchant_reference"), signatureBuilder.toString());
        String signature = getEncryptedSignature(signatureBuilder.toString());
        log.info("The tokenization of transaction {}, signature builder's value after applying sha encryption : {}", requestMap.get("merchant_reference"), signature);
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


    /*public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println("generatedsecureHash: " + 111);
        System.out.println("-----------------------");
    }*/

    /*@Scheduled(initialDelay = 1000 * 10, fixedDelay=Long.MAX_VALUE)
    public void testTokenization() throws JSONException, JsonProcessingException {
        initPayment(UUID.randomUUID().toString(), "2105", "4005550000000001", "123", "Ahmed Bouzaien");
    }*/

}
