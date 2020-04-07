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
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.PayFortOperationDTO;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
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

    @Inject
    PaymentService paymentService;

    @Inject
    PaymentMethodService paymentMethodService;

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
    private String processPaymentUrl;
    @Value("${tbs.payment.payfort-payment-notification}")
    private String paymentNotificationUrl;


    public PayFortOperationDTO initPayment(Long invoiceNumber) throws UnsupportedEncodingException {
        log.info("Request to initiate Payment : {}", invoiceNumber);
        DateFormat df = new SimpleDateFormat("HHmmss");
        String transactionId = invoiceNumber.toString() + df.format(new Timestamp(System.currentTimeMillis()));

        Optional<Invoice> invoice = invoiceRepository.findByAccountId(invoiceNumber);
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Invoice does not exist");
        } else if (invoice.get().getPaymentStatus() == PaymentStatus.PAID) {
            throw new TbsRunTimeException("Invoice already paid");
        } else if (invoice.get().getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new TbsRunTimeException("Invoice already refunded");
        } else if (invoice.get().getStatus() == InvoiceStatus.EXPIRED) {
            throw new TbsRunTimeException("Invoice already expired");
        }

        Payment payment = Payment.builder().build();//paymentMapper.toEntity(paymentDTO);
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(Constants.CREDIT_CARD);
        payment.setPaymentMethod(paymentMethod.get());
        payment.setInvoice(invoice.get());
        payment.setAmount(invoice.get().getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(transactionId);

        paymentRepository.save(payment);

        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String, String> map = new TreeMap();
        map.put("service_command",Constants.PaymentOperation.TOKENIZATION.name());
        map.put("access_code",accessCode);
        map.put("merchant_identifier",merchantIdentifier);
        map.put("merchant_reference",transactionId);
        map.put("language",language);
        map.put("return_url",processPaymentUrl);

        PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
            .serviceCommand(Constants.PaymentOperation.TOKENIZATION.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(transactionId)
            .language(language)
            .returnUrl(processPaymentUrl)
            .build();
        payfortOperationRequest.setSignature(calculatePayfortRequestSignatureForTokenization(map));

        return payfortOperationRequest;
    }

   /* public ResponseEntity<PayFortOperationDTO> doPaymentOperation(Map<String, String> params, HttpServletRequest request) {

        if (params != null && Constants.PaymentOperation.TOKENIZATION.name().equals(params.get("service_command"))) {
            return proceedPaymentOperation(params, request);
        } else if (params != null && Constants.PaymentOperation.PURCHASE.name().equals(params.get("command"))) {
            return checkPaymentResult(params);
        } else {
            throw new TbsRunTimeException("Unknown or unsupported payment operation");
        }
    }*/

    // PayFort resp: after Tokenization
    // response_code=18000&card_number=400555******0001&card_holder_name=Ahmed+B
    // &signature=07793d77079cc89a769281ed55d3237b71583c76172c6350cfe3ed1f24304621
    // &merchant_identifier=e93bbe3b&expiry_date=2105&access_code=D3KyGokx8hLlQmOVszty
    // &language=en&service_command=TOKENIZATION
    // &response_message=Success&merchant_reference=7000000360021648
    // &token_name=94c729e0864d413687035ac2fe4add31 &return_url=http%3A%2F%2Flocalhost%3A9000%2Fapi%2Fpayments%2Fpayfort-processing
    //&card_bin=400555&status=18
    public ResponseEntity<PayFortOperationDTO> proceedPaymentOperation(Map<String, String> params, HttpServletRequest request) {
        if (params == null || !Constants.PaymentOperation.TOKENIZATION.name().equals(params.get("service_command"))) {
            throw new TbsRunTimeException("Unknown or unsupported payment operation");
        }
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
            // .customerIp("192.178.1.10") // detect public ip
            .customerIp(request.getRemoteAddr())
            .tokenName(params.get("token_name"))
            // .signature() set below
            // .paymentOption("VISA") // pass list? MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            // .eci("ECOMMERCE")
            .orderDescription("Test integration")
            .customerName("Ahmed B")
            .settlementReference(params.get("merchant_reference"))
            .returnUrl(paymentNotificationUrl)
            .build();
        Map<String, String> map = new TreeMap();
        map.put("command", Constants.PaymentOperation.PURCHASE.name());
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
        log.debug("Purchase request status: {}, description ", result.getBody().getStatus(), result.getBody().getResponseMessage());

        // Must change status to checkout page

        return result;
    }


    public void processPaymentNotification(HttpServletRequest request, HttpServletResponse response, Map<String, String> params) {
        // PayFort resp: after Purchase
        // response_code=14000&card_holder_name=Ahmed%20B&signature=21b2314fc360366fdcceeab354391c7d311c35e02aef4641af874e28e74cf1e7
        // &merchant_identifier=e93bbe3b&access_code=D3KyGokx8hLlQmOVszty&order_description=Test%20integration
        // &customer_ip=192.178.1.10&language=en&eci=ECOMMERCE&merchant_reference=7000000359194746
        // &authorization_code=289838&token_name=147bac82286d4e5fa50ab48ab182b4e7&currency=SAR&amount=10000
        // &card_number=400555******0001&payment_option=VISA&expiry_date=2105&fort_id=158627809200086182
        // &command=PURCHASE&response_message=Success&customer_email=a.bouzaien.@tamkeentech.sa
        // &settlement_reference=7000000359194746&customer_name=Ahmed%20B&status=14
        if (params == null || !Constants.PaymentOperation.PURCHASE.name().equals(params.get("command"))) {
            // throw new TbsRunTimeException("Unknown or unsupported payment operation");
            // ToDo save fail and redirect to client
        }
        String transactionId = params.get("merchant_reference");
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new PaymentGatewayException("Payfort notification, Payment not found");
        }
        Invoice invoice = payment.getInvoice();
        PaymentStatusResponseDTO.PaymentStatusResponseDTOBuilder paymentStatusResp = PaymentStatusResponseDTO.builder()
            .code(params.get("status"))
            .billNumber(invoice.getAccountId().toString())
            .transactionId(transactionId).description(params.get("response_message"))
            .cardNumber(params.get("card_number")).cardExpiryDate(params.get("expiry_date"))
            .amount(params.get("amount"));
        /*if (!generatedsecureHash.equals(receivedSecurehash)) {
            // IF they are not equal then the response shall not be accepted
            log.error("--<<>>-- processPaymentNotification: Received Secure Hash does not Equal Generated Secure hash");
        } else */{
            // Complete the Action get other parameters from result map and do your processes
            // Please refer to The Integration Manual to see the List of The Received Parameters
            log.info("Status is: {}", params.get("status"));
            paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResp.build(), payment);
        }
        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + transactionId;
        log.info("------Redirect after payment to: {}", redirectUrl);
        response.addHeader("Location", redirectUrl);
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
