package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
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
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class STCPaymentService {

    @Value("${tbs.payment.stc-direct-payment-authorize}")
    private String stcDirectPaymentAuthorize;

    @Value("${tbs.payment.stc-direct-payment}")
    private String stcDirectPayment;

    @Value("${tbs.payment.stcPay-url-form}")
    private String urlForm;

    @Value("${tbs.payment.stcPay-return-url}")
    private String returnUrl;

    @Inject
    private LanguageUtil languageUtil;

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;


    public STCPaymentService(ObjectMapper objectMapper, PaymentService paymentService, PaymentMapper paymentMapper, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
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
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcDirectPaymentAuthorize);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));
        HttpResponse response;
        response = client.execute(post);
        STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(response.getEntity().getContent(), STCPayDirectPaymentAuthorizeRespDTO.class);

        payment.setOtpReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        payment.setPaymentReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        paymentService.save(paymentMapper.toDto(payment));

        model.addAttribute("transactionId", payment.getTransactionId());
        model.addAttribute("codeInvalid", languageUtil.getMessageByKey("stc.code.invalid", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("optValue", languageUtil.getMessageByKey("stc.optValue.label", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("formTitle", languageUtil.getMessageByKey("stc.form.title", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
        model.addAttribute("cardPay", languageUtil.getMessageByKey("payment.card.pay", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));
//      model.addAttribute("actionUrl", urlForm);
        model.addAttribute("return_url", returnUrl);


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
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(stcDirectPayment);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));

        HttpResponse response1;
        response1 = client.execute(post);
        STCPayDirectPaymentRespDTO stcPayRes = objectMapper.readValue(response1.getEntity().getContent(), STCPayDirectPaymentRespDTO.class);

        if(stcPayRes.getDirectPaymentV4ResponseMessage().getPaymentStatus() == 0){

        }



    }
}
