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
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.service.dto.StcDTO.STCPayDirectPaymentAuthorizeRespDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;

import java.io.IOException;

@Service
public class STCPaymentService {

    @Value("${tbs.payment.stc-direct-payment}")
    private String stcDirectPayment;

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;


    public STCPaymentService(ObjectMapper objectMapper, PaymentService paymentService, PaymentMapper paymentMapper) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
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
        HttpPost post = new HttpPost(stcDirectPayment);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(stcPayReqObj.toString()));
        HttpResponse response;
        response = client.execute(post);
        STCPayDirectPaymentAuthorizeRespDTO stcPayRes = objectMapper.readValue(response.getEntity().getContent(), STCPayDirectPaymentAuthorizeRespDTO.class);

        payment.setOtpReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        payment.setPaymentReference(stcPayRes.getDirectPaymentAuthorizeV4ResponseMessage().getOtpReference());
        paymentService.save(paymentMapper.toDto(payment));

        model.addAttribute("merchant_reference", testId);

        return "paymentIframeSTC";

    }
}
