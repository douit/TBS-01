package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayDirectPaymentAuthorizeRespDTO;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayDirectPaymentRespDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;
import sa.tamkeentech.tbs.service.util.CommonUtils;
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;

import javax.inject.Inject;
import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

@Service
public class STCPaymentService {

    @Value("${tbs.payment.stc-direct-payment-authorize}")
    private String stcDirectPaymentAuthorize;

    @Value("${tbs.payment.stc-direct-payment}")
    private String stcDirectPayment;

    @Value("${tbs.payment.stcPay-url-form}")
    private String urlForm;

    @Value("${tbs.payment.stcPay-key-store-password}")
    private String keyStorePassword;

    @Value("${tbs.payment.stcPay-key-store}")
    private String keyStoreFile;

    @Inject
    private LanguageUtil languageUtil;

    @Inject
    @Lazy
    private PaymentService paymentService;

    @Autowired
    private Environment environment;

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
        if (CommonUtils.isProfile(environment, "prod")) {
            // ToDO format the mobile 966---- otherwise error
            stcPayReqParam.put("MobileNo", payment.getInvoice().getCustomer().getContact().getPhone());
        } else {
            stcPayReqParam.put("MobileNo", "966539396141");
        }
        stcPayReqParam.put("Amount", payment.getAmount());
        stcPayReqParam.put("MerchantNote", testId);
        stcPayReqObj.put("DirectPaymentAuthorizeV4RequestMessage", stcPayReqParam);

        // STC Resp as string
        String res = httpsStcRequest(stcPayReqObj.toString(), stcDirectPaymentAuthorize);

        if (StringUtils.isNotEmpty(res)) {
            STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(res, STCPayDirectPaymentAuthorizeRespDTO.class);
            payment.setOtpReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
            payment.setPaymentReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getSTCPayPmtReference());
            paymentRepository.save(payment);
        } else {
            log.error("------STC Authorize failed - empty body");
        }

        model.addAttribute("transactionId", payment.getTransactionId());
        model.addAttribute("codeInvalid", languageUtil.getMessageByKey("stc.code.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("optValue", languageUtil.getMessageByKey("stc.optValue.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("formTitle", languageUtil.getMessageByKey("stc.form.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardPay", languageUtil.getMessageByKey("payment.card.pay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("actionUrl", urlForm);
        model.addAttribute("currentLang", lang.equalsIgnoreCase(Constants.DEFAULT_HEADER_LANGUAGE)? "ar": "en");

        return "paymentIframeSTC";

    }

    public void proceedPaymentOperation(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {

        Payment payment = paymentRepository.findByTransactionId(params.get("merchant_reference").toString());
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
        String testId = "0000000";
        stcPayReqParam.put("BranchID", testId);
        stcPayReqParam.put("TellerID", testId);
        stcPayReqParam.put("RefNum", payment.getTransactionId());
        stcPayReqParam.put("BillNumber", payment.getInvoice().getAccountId());
        stcPayReqParam.put("BillDate", payment.getExpirationDate());
        stcPayReqParam.put("Amount", payment.getAmount());
        stcPayReqParam.put("MerchantNote", testId);
        stcPayReqParam.put("TokenId", testId);
        stcPayReqObj.put("DirectPaymentV4RequestMessage", stcPayReqParam);
        /*HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcDirectPayment);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));

        HttpResponse response1;
        response1 = client.execute(post);*/

        // STC Resp as string
        String res = httpsStcRequest(stcPayReqObj.toString(), stcDirectPaymentAuthorize);

        if (StringUtils.isNotEmpty(res)) {
            STCPayDirectPaymentRespDTO stcPayRes = objectMapper.readValue(res, STCPayDirectPaymentRespDTO.class);
            if(stcPayRes.getDirectPaymentV4ResponseMessage().getPaymentStatus() == 0){

            }
        } else {
            log.error("------STC Payment failed - empty body");
        }
        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + payment.getTransactionId();
        response.addHeader("Location", redirectUrl);
        response.setStatus(HttpStatus.FOUND.value());
    }

    private String httpsStcRequest(String requestBody, String requestUrl) {
        // HTTPS
        // STC Resp as string
        StringBuilder sb = new StringBuilder();
        try {
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            // KeyStore clientStore = KeyStore.getInstance("JKS");
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
            URL url = new URL(requestUrl);
            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            urlConn.setRequestProperty("Content-Type", "application/json");
            // urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("X-ClientCode", "61248102687");
            urlConn.setRequestMethod("POST");
            OutputStreamWriter wr = new OutputStreamWriter
                (urlConn.getOutputStream());
            wr.write(requestBody);
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
            } else {
                // error from STC
                log.error("------STC call failed: {}", HttpResult);
            }
        } catch (Exception e) {
            log.error("------STC call exception: {}", e);
        }
        return sb.toString();
    }
}
