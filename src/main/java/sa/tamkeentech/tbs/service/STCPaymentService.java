package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.RefundStatusCCResponseDTO;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayDirectPaymentAuthorizeRespDTO;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayDirectPaymentRespDTO;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayPaymentInquiryRespDTO;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayPaymentRefundRespDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;
import sa.tamkeentech.tbs.service.util.CommonUtils;
import sa.tamkeentech.tbs.service.util.LanguageUtil;

import javax.inject.Inject;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class STCPaymentService {

    @Value("${tbs.payment.stcpay-direct-payment-authorize}")
    private String stcDirectPaymentAuthorize;

    @Value("${tbs.payment.stcpay-direct-payment}")
    private String stcDirectPayment;

    @Value("${tbs.payment.stcpay-url-form}")
    private String urlForm;

    @Value("${tbs.payment.stcpay-key-store-password}")
    private String keyStorePassword;

    @Value("${tbs.payment.stcpay-key-store}")
    private String keyStoreFile;

    @Value("${tbs.payment.stcpay-payment-inquiry}")
    private String stcPaymentInquiry;

    @Value("${tbs.payment.stcpay-refund}")
    private String stcPayRefund;

    @Inject
    private LanguageUtil languageUtil;

    @Inject
    @Lazy
    private PaymentService paymentService;

    @Autowired
    private Environment environment;

    @Inject
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final Logger log = LoggerFactory.getLogger(PaymentService.class);


    public STCPaymentService(ObjectMapper objectMapper, PaymentMapper paymentMapper, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.objectMapper = objectMapper;
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // return the form to confirm/update mobile
    public  String preparePayment(Model model, Payment payment, String lang) throws JSONException, IOException {
        String mobile= payment.getInvoice().getCustomer().getContact().getPhone();
        if (mobile != null) {
            //00966539396141
            if (mobile.length() == 14 && mobile.startsWith("009665")) {
                mobile = "0" + mobile.substring(5);
            } else if (mobile.length() == 13 && mobile.startsWith("+9665")) {
                // +966539396141
                mobile = "0" + mobile.substring(4);
            } else if (mobile.length() == 10 && mobile.startsWith("05")) {
                // 0539396141 --> correct
            } else {
                // wrong number
                mobile = "";
            }
        } else {
            mobile = "";
        }

        // if (CommonUtils.isProfile(environment, "prod")) {
        model.addAttribute("MobileNo", mobile);
        model.addAttribute("mobileValidationStatus", Constants.STCPayMobileValidationStatus.UNSET);
        /*} else {
            model.addAttribute("MobileNo", "966539396141");
        }*/
        model.addAttribute("transactionId", payment.getTransactionId());
        model.addAttribute("codeInvalid", languageUtil.getMessageByKey("stc.code.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("optLabel", languageUtil.getMessageByKey("stc.optValue.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("formTitle", languageUtil.getMessageByKey("stc.form.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("submitButtonLabel", languageUtil.getMessageByKey("payment.card.submit", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("actionUrl", urlForm);
        model.addAttribute("currentLang", lang.equalsIgnoreCase(Constants.DEFAULT_HEADER_LANGUAGE)? "ar": "en");
        model.addAttribute("mobileLabel", languageUtil.getMessageByKey("stc.mobile.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));

        // all labels -- needed for otp and pay
        model.addAttribute("payButtonLabel", languageUtil.getMessageByKey("payment.card.pay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("mobileInvalid", languageUtil.getMessageByKey("stc.mobile.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("codeInvalid", languageUtil.getMessageByKey("stc.code.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("optLabel", languageUtil.getMessageByKey("stc.optValue.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("formTitle", languageUtil.getMessageByKey("stc.form.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));

        return "paymentIframeSTC";
    }

    // return the form to enter otp
    public ResponseEntity<Object> initPayment(Map<String, String> payload) throws JSONException, IOException {

        Payment payment = paymentRepository.findByTransactionId(payload.get("merchant_reference"));
        String mobile = payload.get("mobile_no");
        // resp
        Map<String, String> model = new HashMap<>();

        JSONObject stcPayReqParam = new JSONObject();
        JSONObject stcPayReqObj = new JSONObject();
        String clientName= payment.getInvoice().getClient().getName();
        stcPayReqParam.put("BranchID", clientName);
        stcPayReqParam.put("TellerID", clientName);
        stcPayReqParam.put("DeviceID", clientName);
        stcPayReqParam.put("RefNum", payment.getTransactionId());
        stcPayReqParam.put("BillNumber", payment.getInvoice().getAccountId());
        // ToDO Get Mobile form
        //if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "ahmed")) {
            stcPayReqParam.put("MobileNo", "966" + mobile.substring(1));
        /*} else {
            stcPayReqParam.put("MobileNo", "966539396141");
        }*/
        stcPayReqParam.put("Amount", payment.getAmount());
        // stcPayReqParam.put("MerchantNote", "");
        stcPayReqObj.put("DirectPaymentAuthorizeV4RequestMessage", stcPayReqParam);

        // STC Resp as string
        String res = httpsStcRequest(stcPayReqObj.toString(), stcDirectPaymentAuthorize);


        try {
            if (StringUtils.isNotEmpty(res)) {
                // success case {"DirectPaymentAuthorizeV4ResponseMessage":{"OtpReference":"UMSCPwTVAnJXZhDJHeQO","STCPayPmtReference":"1262678035888","ExpiryDuration":300}}
                // error 403 case "{"Code":2008,"Text":"Customer not found","Type":0}",
                STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(res, STCPayDirectPaymentAuthorizeRespDTO.class);
                payment.setOtpReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
                payment.setPaymentReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getSTCPayPmtReference());
                paymentRepository.save(payment);
                model.put("mobileValidationStatus", Constants.STCPayMobileValidationStatus.VALID.name());

            } else {
                log.error("------STC Authorize failed - empty body");
                model.put("mobileValidationStatus", Constants.STCPayMobileValidationStatus.INVALID.name());
            }
        } catch (Exception e) {
            log.error("------STC Authorize failed - can't extract otp ");
            model.put("mobileValidationStatus", Constants.STCPayMobileValidationStatus.INVALID.name());
        }

        if (model.get("mobileValidationStatus").equals(Constants.STCPayMobileValidationStatus.VALID.name())) {
            return ResponseEntity.status(HttpStatus.OK).build();//.body(model);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();//.body(model);
    }

    public void proceedPaymentOperation(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response, Map<String, String> payload) throws JSONException, IOException {

        // payload.get("merchant_reference") in case of mobile validation
        // params.get("merchant_reference") in case of payment submit
        Payment payment = paymentRepository.findByTransactionId(params.get("merchant_reference").toString());

        // submit otp
        payment.setStatus(PaymentStatus.CHECKOUT_PAGE);
        paymentRepository.save(payment);

        Invoice invoice = payment.getInvoice();
        invoice.setPaymentStatus(PaymentStatus.CHECKOUT_PAGE);
        invoiceRepository.save(invoice);

        JSONObject stcPayReqParam = new JSONObject();
        JSONObject stcPayReqObj = new JSONObject();
        stcPayReqParam.put("STCPayPmtReference", payment.getPaymentReference());
        stcPayReqParam.put("OtpReference", payment.getOtpReference());
        stcPayReqParam.put("OtpValue", params.get("opt_value").toString());
        stcPayReqParam.put("TokenizeYn", "false");
        stcPayReqParam.put("TokenReference", "");
        stcPayReqObj.put("DirectPaymentConfirmV4RequestMessage", stcPayReqParam);

        // STC Resp as string
        String res = httpsStcRequest(stcPayReqObj.toString(), stcDirectPayment);

        if (StringUtils.isNotEmpty(res)) {
            STCPayDirectPaymentRespDTO stcPayRes = objectMapper.readValue(res, STCPayDirectPaymentRespDTO.class);
            // Possible values:  Pending = 1,   Paid = 2,   Cancelled = 4,  Expired = 5    --- 6 other errors like balance
            // NPE STC pay request status code: 200 OK, body {"Code":2023,"Text":"Available balance is not enough to proceed","Type":0}  or 200 OK, body {"Code":2031,"Text":"Invalid OTP value","Type":0}
            if (stcPayRes != null && stcPayRes.getDirectPaymentV4ResponseMessage() != null && stcPayRes.getDirectPaymentV4ResponseMessage().getPaymentStatus() != null) {
                updatePaymentStatus(payment, invoice, stcPayRes.getDirectPaymentV4ResponseMessage().getPaymentStatus());
            } else {
                updatePaymentStatus(payment, invoice, 6);
            }
        } else {
            log.error("------STC Payment failed - empty body");
        }
        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + payment.getTransactionId();
        response.addHeader("Location", redirectUrl);
        response.setStatus(HttpStatus.FOUND.value());
        // return null;
    }

    private String httpsStcRequest(String requestBody, String requestUrl) {
        // HTTPS
        // STC Resp as string
        StringBuilder sb = new StringBuilder();

        // direct call working --> move to mule
        /*try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            clientStore.load(STCPaymentService.class.getClassLoader().getResourceAsStream("config/tls/Tkeen-win-gen.pfx"),keyStorePassword.toCharArray());


            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.setProtocol("TLS");
            sslContextBuilder.loadKeyMaterial(clientStore, keyStorePassword.toCharArray());
            sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build());
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();


            HttpPost httpPost = new HttpPost("https://b2b.stcpay.com.sa/B2B.DirectPayment.WebApi/DirectPayment/V4/DirectPaymentAuthorize");
            StringEntity entity = new StringEntity(requestBody);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("X-ClientCode", "74133136252");

            httpPost.setEntity(entity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if(response.getEntity() != null) {
                return  EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error("------STC call exception: {}", e);
        }
        return sb.toString();*/

        // calling mule
        String resp = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<String>(requestBody, headers);
            ResponseEntity<String> result = restTemplate.postForEntity(requestUrl, entity, String.class);
            log.debug("STC pay request status code: {}, body {}", result.getStatusCode(), result.getBody());
            resp = result.getBody();
        } catch (Exception e) {
            log.error("------STC call exception: {}", e);
        }

        return resp;
    }

    public boolean checkOfflilnePaymentStatus(Payment payment) {
        try {
            JSONObject stcPayInqReqParam = new JSONObject();
            JSONObject stcPayInqReqObj = new JSONObject();
            stcPayInqReqParam.put("RefNum", payment.getTransactionId());
            stcPayInqReqParam.put("PaymentsDate", payment.getCreatedDate());
            stcPayInqReqObj.put("PaymentInquiryV4RequestMessage", stcPayInqReqParam);

            // STC Resp as string
            String res = httpsStcRequest(stcPayInqReqObj.toString(), stcPaymentInquiry);

            if (StringUtils.isNotEmpty(res)) {
                STCPayPaymentInquiryRespDTO stcPayInqRes = objectMapper.readValue(res, STCPayPaymentInquiryRespDTO.class);
                if (stcPayInqRes != null && stcPayInqRes.getPaymentInquiryV4ResponseMessage() != null && CollectionUtils.isNotEmpty(stcPayInqRes.getPaymentInquiryV4ResponseMessage().getTransactionList())) {
                    for (STCPayPaymentInquiryRespDTO.TransactionList transaction : stcPayInqRes.getPaymentInquiryV4ResponseMessage().getTransactionList()) {
                        // Possible values:                Pending = 1,          Paid = 2,                Cancelled = 4,          Expired = 5
                        log.debug("STC pay payment correction {}", payment.getInvoice().getAccountId());
                        Boolean isSuccessfulOp = updatePaymentStatus(payment, payment.getInvoice(), transaction.getPaymentStatus());
                        if (isSuccessfulOp != null && isSuccessfulOp) {
                            return true;
                        }
                    }//);
                }

            } else {
                log.error("------STC inquiry failed - empty body");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Boolean updatePaymentStatus(Payment payment, Invoice invoice, int status) {
        // Possible values:  Pending = 1,   Paid = 2,   Cancelled = 4,  Expired = 5
        if(status == 2) {
            log.debug("------STC Payment {} Success - updating payment and invoice", payment.getTransactionId());
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
            invoice.setPaymentStatus(PaymentStatus.PAID);
            invoiceRepository.save(invoice);
            return Boolean.TRUE;
        } else if (status != 1) {
            log.debug("------STC Payment {} failed with status:{}", payment.getTransactionId(), status);
            payment.setStatus(PaymentStatus.UNPAID);
            paymentRepository.save(payment);
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
            invoiceRepository.save(invoice);
        } else {
            // keep chckout_page status --> job retry
            log.debug("------STC Payment {} pending with status:{}", payment.getTransactionId(), status);
        }
        return null;
    }


    public RefundStatusCCResponseDTO proceedRefundOperation(Refund refund, Invoice invoice, Optional<Payment> payment) throws JSONException, IOException {

        RefundStatusCCResponseDTO refundStatusCCResponseDTO =  RefundStatusCCResponseDTO.builder()
            .refundId(refund.getPayment().getTransactionId()).build();

        JSONObject stcPayRefReqParam = new JSONObject();
        JSONObject stcPayRefReqObj = new JSONObject();
        stcPayRefReqParam.put("STCPayRefNum", payment.get().getPaymentReference());
        stcPayRefReqParam.put("Amount", payment.get().getAmount());
        stcPayRefReqObj.put("RefundPaymentRequestMessage", stcPayRefReqParam);

        // STC Resp as string
        String res = httpsStcRequest(stcPayRefReqObj.toString(), stcPayRefund);
        // The only success response if 200 OK == body not empty
        if (StringUtils.isNotEmpty(res)) {
            STCPayPaymentRefundRespDTO stcPayRefRes = objectMapper.readValue(res, STCPayPaymentRefundRespDTO.class);
            if (stcPayRefRes != null ) {
                payment.get().setPaymentReference(stcPayRefRes.getRefundPaymentResponseMessage().getNewSTCPayRefNum());
                payment.get().setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment.get());
                invoice.setPaymentStatus(PaymentStatus.REFUNDED);
                invoiceRepository.save(invoice);
                refundStatusCCResponseDTO.setStatus(RequestStatus.SUCCEEDED);
            }else{
                refundStatusCCResponseDTO.setStatus(RequestStatus.FAILED);

            }
        }


        return refundStatusCCResponseDTO;
    }
}
