package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentProvider;
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
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;

import javax.inject.Inject;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.List;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    @Value("${tbs.payment.stcPay-key-store-password}")
    private String keyStorePassword;

    @Value("${{tbs.payment.stcPay-key-store}")
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

    public  String initPayment(Model model, Payment payment, String lang) throws JSONException, IOException {

        JSONObject stcPayReqParam = new JSONObject();
        JSONObject stcPayReqObj = new JSONObject();
        String testId = "0000000";
        stcPayReqParam.put("BranchID", testId);
        stcPayReqParam.put("TellerID", testId);
        stcPayReqParam.put("DeviceID", testId);
        stcPayReqParam.put("RefNum", payment.getTransactionId());
        stcPayReqParam.put("BillNumber", payment.getInvoice().getAccountId());
        stcPayReqParam.put("MobileNo", payment.getInvoice().getCustomer().getContact().getPhone());
        stcPayReqParam.put("Amount", payment.getAmount());
        stcPayReqParam.put("MerchantNote", testId);
        stcPayReqObj.put("DirectPaymentAuthorizeV4RequestMessage", stcPayReqParam);



        // HTTPS
        // STC Resp as string
        StringBuilder sb = new StringBuilder();
        try {
            KeyStore clientStore = KeyStore.getInstance("JKS");
            clientStore.load(STCPaymentService.class.getClassLoader().getResourceAsStream(keyStoreFile), keyStorePassword.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(clientStore, keyStorePassword.toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");

            sslContext.init(kms, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            URL url = new URL(stcDirectPaymentAuthorize);
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("X-ClientCode", "61248102687");
            urlConn.setRequestMethod("POST");
            OutputStreamWriter wr = new OutputStreamWriter
                (urlConn.getOutputStream());
            wr.write(stcPayReqObj.toString());
            wr.flush();
            // StringBuilder sb = new StringBuilder();
            int HttpResult = urlConn.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return sb.toString();
            } else {
                // error from STC
                log.error("------STC Authorize failed: {}", HttpResult);
            }
        } catch (Exception e) {
            log.error("------STC Authorize exception: {}", e);
        }
        // End HTTPS


        /*HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcDirectPaymentAuthorize);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));
        HttpResponse response;
        response = client.execute(post);
        STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(response.getEntity().getContent(), STCPayDirectPaymentAuthorizeRespDTO.class);*/
        STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(sb.toString(), STCPayDirectPaymentAuthorizeRespDTO.class);

        payment.setOtpReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        payment.setPaymentReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        paymentService.save(paymentMapper.toDto(payment));

        model.addAttribute("transactionId", payment.getTransactionId());
        model.addAttribute("codeInvalid", languageUtil.getMessageByKey("stc.code.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("optValue", languageUtil.getMessageByKey("stc.optValue.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("formTitle", languageUtil.getMessageByKey("stc.form.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardPay", languageUtil.getMessageByKey("payment.card.pay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
//      model.addAttribute("actionUrl", urlForm);
        model.addAttribute("return_url", urlForm);


        return "paymentIframeSTC";

    }

    public void proceedPaymentOperation(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {

        Payment payment = paymentRepository.findByTransactionId(params.get("transactionId").toString());
        Invoice invoice = null;
        if (payment != null) {
            payment.setStatus(PaymentStatus.CHECKOUT_PAGE);
            paymentRepository.save(payment);
            invoice = payment.getInvoice();
            invoice.setPaymentStatus(PaymentStatus.CHECKOUT_PAGE);
            invoiceRepository.save(invoice);
        } else {
            throw new PaymentGatewayException("STC prchase, Payment not found, transactionId=" + params.get("transactionId"));
        }


        JSONObject stcPayReqParam = new JSONObject();
        JSONObject stcPayReqObj = new JSONObject();
        String testId = "0000000000";
        stcPayReqParam.put("BranchID", testId);
        stcPayReqParam.put("TellerID", testId);
        stcPayReqParam.put("RefNum", payment.getTransactionId());
        stcPayReqParam.put("BillNumber", payment.getInvoice().getAccountId());
        stcPayReqParam.put("BillDate", payment.getExpirationDate());
        stcPayReqParam.put("Amount", payment.getAmount());
        stcPayReqParam.put("MerchantNote", testId);
        stcPayReqParam.put("TokenId", testId);
        stcPayReqObj.put("DirectPaymentV4RequestMessage", stcPayReqParam);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcDirectPayment);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("X-ClientCode", "61248102687");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));

        HttpResponse response1;
        response1 = client.execute(post);
        STCPayDirectPaymentRespDTO stcPayRes = objectMapper.readValue(response1.getEntity().getContent(), STCPayDirectPaymentRespDTO.class);

        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + params.get("transactionId");

        if (stcPayRes.getDirectPaymentV4ResponseMessage().getPaymentStatus() == 0) {
            payment.setStatus(PaymentStatus.PAID);
            invoice.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.UNPAID);
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        response1.addHeader("Location", redirectUrl);

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

    }

    @Scheduled(cron = "${tbs.cron.stcpay-payment-inquiry}")
    public void paymentInquiry() throws JSONException, IOException {

        List<Optional<Invoice>> invoices = invoiceRepository.findByPaymentStatus(PaymentStatus.CHECKOUT_PAGE);

        for (Optional<Invoice> invoice : invoices) {
            Optional<Payment> payment = paymentRepository.findFirstByInvoiceAccountIdAndStatusAndPaymentProvider(invoice.get().getAccountId(), PaymentStatus.CHECKOUT_PAGE, PaymentProvider.STC_PAY);

            JSONObject stcPayInqReqParam = new JSONObject();
            JSONObject stcPayInqReqObj = new JSONObject();
            stcPayInqReqParam.put("RefNum", payment.get().getTransactionId());
            stcPayInqReqParam.put("PaymentsDate", payment.get().getCreatedDate());
            stcPayInqReqObj.put("PaymentInquiryV4RequestMessage", stcPayInqReqParam);

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(stcPaymentInquiry);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("X-ClientCode", "61248102687");
            post.setEntity(new StringEntity(stcPayInqReqObj.toString()));

            HttpResponse response1;
            response1 = client.execute(post);
            STCPayPaymentInquiryRespDTO stcPayInqRes = objectMapper.readValue(response1.getEntity().getContent(), STCPayPaymentInquiryRespDTO.class);
            if (response1.getStatusLine().getStatusCode() == 200) {
                stcPayInqRes.getPaymentInquiryV4ResponseMessage().getTransactionList().forEach(transaction -> {
                    if (transaction.getPaymentStatus() == 0) {
                        payment.get().setStatus(PaymentStatus.PAID);
                        paymentRepository.save(payment.get());
                        invoice.get().setPaymentStatus(PaymentStatus.PAID);
                        invoiceRepository.save(invoice.get());
                    }
                });
            }

        }
    }

    public RefundStatusCCResponseDTO proceedRefundOperation(Refund refund, Invoice invoice, Optional<Payment> payment) throws JSONException, IOException {

        RefundStatusCCResponseDTO refundStatusCCResponseDTO =  RefundStatusCCResponseDTO.builder()
            .refundId(refund.getPayment().getTransactionId()).build();

        JSONObject stcPayRefReqParam = new JSONObject();
        JSONObject stcPayRefReqObj = new JSONObject();
        stcPayRefReqParam.put("STCPayRefNum", payment.get().getPaymentReference());
        stcPayRefReqParam.put("Amount", payment.get().getAmount());
        stcPayRefReqObj.put("RefundPaymentRequestMessage", stcPayRefReqParam);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcPayRefund);
        post.setHeader("Content-Type", "application/json");
        post.setHeader("X-ClientCode", "61248102687");
        post.setEntity(new StringEntity(stcPayRefReqObj.toString()));

        HttpResponse response1;
        response1 = client.execute(post);
        STCPayPaymentRefundRespDTO stcPayRefRes = objectMapper.readValue(response1.getEntity().getContent(), STCPayPaymentRefundRespDTO.class);

        if (response1.getStatusLine().getStatusCode() == 200) {
            payment.get().setPaymentReference(stcPayRefRes.getRefundPaymentResponseMessage().getNewSTCPayRefNum());
            payment.get().setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment.get());
            invoice.setPaymentStatus(PaymentStatus.REFUNDED);
            invoiceRepository.save(invoice);
            refundStatusCCResponseDTO.setStatus(RequestStatus.SUCCEEDED);
        }else{
            refundStatusCCResponseDTO.setStatus(RequestStatus.FAILED);

        }

        return refundStatusCCResponseDTO;
    }
}
