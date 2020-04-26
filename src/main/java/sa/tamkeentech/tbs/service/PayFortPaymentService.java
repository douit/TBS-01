package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.dto.PayFortOperationDTO;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.service.dto.RefundDTO;
import sa.tamkeentech.tbs.service.dto.RefundStatusCCResponseDTO;
import sa.tamkeentech.tbs.service.mapper.RefundMapper;
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@Transactional
public class PayFortPaymentService {

    private final Logger log = LoggerFactory.getLogger(PayFortPaymentService.class);


    @Inject
    @Lazy
    RefundMapper refundMapper;

    @Inject
    @Lazy
    RefundRepository refundRepository;

    @Inject
    @Lazy
    RefundService refundService;

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

    @Inject
    ObjectMapper objectMapper;

    @Inject
    private LanguageUtil languageUtil;

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


    /**
     * Return the credit card form
     * @param model
     * @param payment
     * @param lang
     * @return
     */
    public String initPayment(Model model, Payment payment, String lang) {
        log.info("Request to initiate Payment : {}", payment.getTransactionId());
        Map<String, Object> map = new TreeMap();
        map.put("service_command",Constants.PaymentOperation.TOKENIZATION.name());
        map.put("access_code",accessCode);
        map.put("merchant_identifier",merchantIdentifier);
        map.put("merchant_reference",payment.getTransactionId());
        map.put("language",language);
        map.put("return_url",processPaymentUrl);

        PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
            .serviceCommand(Constants.PaymentOperation.TOKENIZATION.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(payment.getTransactionId())
            .language(language)
            .returnUrl(processPaymentUrl)
            .build();
        payfortOperationRequest.setSignature(calculatePayfortRequestSignature(map, true));


        model.addAttribute("service_command", payfortOperationRequest.getServiceCommand());
        model.addAttribute("access_code", payfortOperationRequest.getAccessCode());
        model.addAttribute("merchant_identifier", payfortOperationRequest.getMerchantIdentifier());
        model.addAttribute("merchant_reference", payfortOperationRequest.getMerchantReference());
        model.addAttribute("language", payfortOperationRequest.getLanguage());
        model.addAttribute("signature", payfortOperationRequest.getSignature());
        model.addAttribute("return_url", payfortOperationRequest.getReturnUrl());
        model.addAttribute("actionUrl", urlForm);

        // Adding language tokens
        model.addAttribute("cardTitle", languageUtil.getMessageByKey("payment.card.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("applePayTitle", languageUtil.getMessageByKey("payment.card.applepay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardNumber", languageUtil.getMessageByKey("payment.card.number", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardCvv", languageUtil.getMessageByKey("payment.card.cvv", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardHolder", languageUtil.getMessageByKey("payment.card.holder", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardPay", languageUtil.getMessageByKey("payment.card.pay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardCvvTooltip", languageUtil.getMessageByKey("payment.card.cvv.tooltip", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardInvalid", languageUtil.getMessageByKey("payment.card.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("currentLang", lang.equalsIgnoreCase(Constants.DEFAULT_HEADER_LANGUAGE)? "ar": "en");

        return "paymentIframePayfort";
    }

    // PayFort resp: after Tokenization
    // response_code=18000&card_number=400555******0001&card_holder_name=Ahmed+B
    // &signature=07793d77079cc89a769281ed55d3237b71583c76172c6350cfe3ed1f24304621
    // &merchant_identifier=e93bbe3b&expiry_date=2105&access_code=D3KyGokx8hLlQmOVszty
    // &language=en&service_command=TOKENIZATION
    // &response_message=Success&merchant_reference=7000000360021648
    // &token_name=94c729e0864d413687035ac2fe4add31 &return_url=http%3A%2F%2Flocalhost%3A9000%2Fapi%2Fpayments%2Fpayfort-processing
    //&card_bin=400555&status=18

    /**
     * Processing Tokenization and Purchase
     * @param params
     * @param request
     * @param response
     */
    public void proceedPaymentOperation(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {

        if (params != null && Constants.PaymentOperation.PURCHASE.name().equals(params.get("command"))) {
            processPaymentNotification(request, response, params);
        } else if (params != null && Constants.PaymentOperation.TOKENIZATION.name().equals(params.get("service_command"))) {
            log.debug("------Payfort payment processing tokenizaion code: {}, message: {}", params.get("status"),  params.get("response_message"));
            // Must change status to checkout page
            Payment payment = paymentRepository.findByTransactionId(params.get("merchant_reference").toString());
            Invoice invoice = null;
            if (payment != null) {
                payment.setStatus(PaymentStatus.CHECKOUT_PAGE);
                paymentRepository.save(payment);

                invoice = payment.getInvoice();
                invoice.setPaymentStatus(PaymentStatus.CHECKOUT_PAGE);
                invoiceRepository.save(invoice);
            } else {
                throw new PaymentGatewayException("Payfort prchase, Payment not found, transactionId=" + params.get("merchant_reference"));
            }
            BigDecimal roundedAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
            PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
                .command(Constants.PaymentOperation.PURCHASE.name())
                .accessCode(accessCode)
                .merchantIdentifier(merchantIdentifier)
                .merchantReference(params.get("merchant_reference").toString())
                .amount(roundedAmount.multiply(new BigDecimal("100")).longValue())// 100 SAR
                .currency("SAR")
                .language(language)
                .customerEmail(invoice.getCustomer().getContact().getEmail())
                // .customerIp("192.178.1.10") // detect public ip
                .customerIp(request.getRemoteAddr())
                // case wrong cc token null
                .tokenName(params.get("token_name") != null? params.get("token_name").toString(): "")
                //.orderDescription("Test integration")
                .customerName(invoice.getCustomer().getName())
                .settlementReference(params.get("merchant_reference").toString())
                .returnUrl(processPaymentUrl)
                .build();
            // arabic desc causes signature mismatch --> use client name
            payfortOperationRequest.setOrderDescription(invoice.getClient().getName() + " Payment");
            Map<String, Object> map = new TreeMap();
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
            map.put("order_description", payfortOperationRequest.getOrderDescription());

            map.put("customer_name", payfortOperationRequest.getCustomerName());
            map.put("settlement_reference", payfortOperationRequest.getSettlementReference());
            map.put("return_url", payfortOperationRequest.getReturnUrl());
            payfortOperationRequest.setSignature(calculatePayfortRequestSignature(map, true));
            log.debug("Purchase request: {}", payfortOperationRequest);


            ResponseEntity<PayFortOperationDTO> result = null;
            String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + params.get("merchant_reference");

            try {
                result = restTemplate.postForEntity(urlJson, payfortOperationRequest, PayFortOperationDTO.class);
                log.debug("Purchase request status: {}, description ", result.getBody().getStatus(), result.getBody().getResponseMessage());
                log.debug("-------Purchase result: {}", result);
                if (StringUtils.isNotEmpty(result.getBody().getUrl3ds())) {
                    response.addHeader("Location", result.getBody().getUrl3ds());
                } else {
                    // redirectUrl += "&status=" + result.getBody().getStatus();
                    log.info("------ Processing ended without 3ds to: {}", redirectUrl);
                    // response.addHeader("Location", redirectUrl);
                    Map<String, Object> paramsresponse = objectMapper.convertValue(result.getBody(), Map.class);
                    processPaymentNotification(request, response, paramsresponse);
                }
            } catch (RestClientException e) {
                log.info("------ Processing issue Redirect after before payment to: {}", redirectUrl);
                response.addHeader("Location", redirectUrl);
                e.printStackTrace();
            }
            response.setStatus(HttpStatus.FOUND.value());
        } else {
            // Add error page
        }
    }


    /**
     * Check Payment status
     * @param request
     * @param response
     * @param params
     */
    public void processPaymentNotification(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
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
        String transactionId = params.get("merchant_reference").toString();
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new PaymentGatewayException("Payfort notification, Payment not found");
        }
        Invoice invoice = payment.getInvoice();
        PaymentStatusResponseDTO paymentStatusResp = PaymentStatusResponseDTO.builder()
            .code((params.get("status") != null)? params.get("status").toString(): null)
            .billNumber(invoice.getAccountId().toString())
            .transactionId(transactionId)
            .description((params.get("response_message") != null)? params.get("response_message").toString(): null)
            .cardNumber((params.get("card_number") != null)? params.get("card_number").toString(): null)
            .cardExpiryDate((params.get("expiry_date") != null)? params.get("expiry_date").toString(): null)
            .amount((params.get("amount") != null)? params.get("amount").toString(): null)
            .build();
        // sort
        Map<String, Object> map = new TreeMap();
        for(Map.Entry<String, Object> entry: params.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        String generatedSecureHash = calculatePayfortRequestSignature(map, false);
        String receivedSecureHash = (params.get("signature") != null)? params.get("signature").toString(): "";
        if (!generatedSecureHash.equals(receivedSecureHash)) {
            // IF they are not equal then the response shall not be accepted
            paymentStatusResp.setCode("00"); // Invalid Request.
            log.error("--<<>>-- processPaymentNotification: Received Secure Hash {} does not Equal Generated Secure hash {}", receivedSecureHash, generatedSecureHash);
        } else {
            // Complete the Action get other parameters from result map and do your processes
            // Please refer to The Integration Manual to see the List of The Received Parameters
            log.info("Status is: {}", params.get("status"));
            paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResp, payment);
        }
        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + transactionId + "&status=" + paymentStatusResp.getCode();
        log.info("------Redirect after payment to: {}", redirectUrl);
        response.addHeader("Location", redirectUrl);
        response.setStatus(HttpStatus.FOUND.value());
    }



    /**
     * Method calculates the signature needed in checkout page
     * @param requestMap
     * @param requestMap
     * @return signature
     */
    private String
    calculatePayfortRequestSignature(Map<String, Object> requestMap, boolean isRequest) {

        String key = (isRequest)?requestPhrase : responsePhrase;
        StringBuilder signatureBuilder = new StringBuilder(key);
        for(Map.Entry<String, Object> entry: requestMap.entrySet()) {
            if (!entry.getKey().equals("signature")) {
                signatureBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        signatureBuilder.append(key);
        log.info("The tokenization of transaction {}, signature builder's value before applying sha encryption : {}", requestMap.get("merchant_reference"), signatureBuilder.toString());
        String signature = getEncryptedSignature(signatureBuilder.toString());
        log.info("The tokenization of transaction {}, signature builder's value after applying sha encryption : {}", requestMap.get("merchant_reference"), signature);
        return signature;
    }

    /**
     *
     * @param signature
     * @return
     */
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


    public RefundStatusCCResponseDTO proceedRefundOperation(Refund refund, Invoice invoice, Optional<Payment> payment){
        log.info("Request to initiate Refund : {}", refund.getId());

        RefundStatusCCResponseDTO refundStatusCCResponseDTO =  RefundStatusCCResponseDTO.builder()
            .refundId(refund.getPayment().getTransactionId()).build();

        BigDecimal roundedAmount = refund.getRefundValue().setScale(2, RoundingMode.HALF_UP);
        PayFortOperationDTO payfortOperationRequest = PayFortOperationDTO.builder()
            .command(Constants.PaymentOperation.REFUND.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(payment.get().getTransactionId())
            .amount(roundedAmount.multiply(new BigDecimal("100")).longValue())// 100 SAR
            .currency("SAR")
            .language(language)
            .build();

        Map<String, Object> map = new TreeMap();
        map.put("command",Constants.PaymentOperation.REFUND.name());
        map.put("access_code",accessCode);
        map.put("merchant_identifier",merchantIdentifier);
        map.put("merchant_reference",payment.get().getTransactionId());
        map.put("amount", payfortOperationRequest.getAmount());
        map.put("currency","SAR");
        map.put("language",language);

        payfortOperationRequest.setSignature(calculatePayfortRequestSignature(map, true));

        ResponseEntity<PayFortOperationDTO> result = null;
        try {
            result = restTemplate.postForEntity(urlJson, payfortOperationRequest, PayFortOperationDTO.class);
            log.debug("Refund request status: {}, description ", result.getBody().getStatus(), result.getBody().getResponseMessage());
            if (result.getBody().getStatus().equals("06")) {
                refundStatusCCResponseDTO.setStatus(RequestStatus.SUCCEEDED);

            } else {
                refundStatusCCResponseDTO.setStatus(RequestStatus.FAILED);
            }
        } catch (RestClientException e) {
            log.info("------ Refund Processing Exception: {}");
        }

        return refundStatusCCResponseDTO;

    }

    public PaymentStatusResponseDTO checkOffilnePaymentStatus(String transactionId ){
        PayFortOperationDTO payfortOperationRequest =  PayFortOperationDTO.builder()
            .queryCommand(Constants.PaymentOperation.CHECK_STATUS.name())
            .accessCode(accessCode)
            .merchantIdentifier(merchantIdentifier)
            .merchantReference(transactionId)
            .language(language)
            .build();


        Map<String, Object> map = new TreeMap();
        map.put("query_command",Constants.PaymentOperation.REFUND.name());
        map.put("access_code",accessCode);
        map.put("merchant_identifier",merchantIdentifier);
        map.put("merchant_reference",transactionId);
        map.put("language",language);

        payfortOperationRequest.setSignature(calculatePayfortRequestSignature(map, true));


        ResponseEntity<PayFortOperationDTO> result = null;
//        try {
//            result = restTemplate.postForEntity(urlJson, payfortOperationRequest, PayFortOperationDTO.class);
//            log.debug("Refund request status: {}, description ", result.getBody().getStatus(), result.getBody().getResponseMessage());
//
//            if (result.getBody().getStatus().equals(06)) {
//                refund.setStatus(RequestStatus.SUCCEEDED);
//                payment.get().setStatus(PaymentStatus.REFUNDED);
//                invoice.setPaymentStatus(PaymentStatus.REFUNDED);
//                paymentRepository.save(payment.get());
//                invoiceRepository.save(invoice);
//
//            } else {
//                refund.setStatus(RequestStatus.FAILED);
//            }
//        } catch (RestClientException e) {
//            log.info("------ Refund Processing Exception: {}");
//        }
//
        PaymentStatusResponseDTO paymentStatusResponseDTO = PaymentStatusResponseDTO.builder()
            .code("")
//            .cardNumber(result.get("Response.CardNumber"))
//            .transactionId(result.get("Response.TransactionID"))
//            .cardHolderName(result.get("Response.CardHolderName"))
//            //.billNumber(result.get())
//            .cardExpiryDate(result.get("Response.CardExpiryDate"))
//            .description(result.get("Response.StatusDescription"))
            .build();
        Payment payment = paymentRepository.findByTransactionId(transactionId);

        paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResponseDTO, payment);
        return paymentStatusResponseDTO;
    }

    /*private PayFortOperationDTO initPayment() throws UnsupportedEncodingException {
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
        Map<String, Object> map = new TreeMap();
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
        payfortOperationRequest.setSignature(calculatePayfortRequestSignature(map, true));

        return payfortOperationRequest;
    }*/


}
