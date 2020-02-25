package sa.tamkeentech.tbs.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional
public class CreditCardPaymentService {

    private final Logger log = LoggerFactory.getLogger(CreditCardPaymentService.class);

    @Inject
    @Lazy
    PaymentRepository paymentRepository;

    @Inject
    PaymentService paymentService;

    @Value("${tbs.payment.sts-secret-key}")
    private String stsSecretKey;
    @Value("${tbs.payment.sts-merchant-id}")
    private String stsMerchantId;
    @Value("${tbs.payment.sts-post-form-url}")
    private String stsPostFormUrl;
    @Value("${tbs.payment.sts-response-back-url}")
    private String stsResponseBackUrl;

    public String initPayment(Model model, String transactionId) {
        log.info("Request to initiate Payment : {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new TbsRunTimeException("Payment not found");
        }
        Invoice invoice = payment.getInvoice();
        // Step 1: Generate Secure Hash
        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String,String> parameters = new TreeMap<>();
        // String transactionId = String.valueOf(System.currentTimeMillis());
        // fill required parameters
        parameters.put("TransactionID", transactionId);
        parameters.put("MerchantID", stsMerchantId);
        BigDecimal roundedAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
        parameters.put("Amount", roundedAmount.multiply(new BigDecimal("100")).toBigInteger().toString());
        parameters.put("CurrencyISOCode", "682");
        parameters.put("ItemID", "181");
        parameters.put("MessageID", "1");
        parameters.put("Quantity", "1");
        parameters.put("Channel", "0");
        // fill some optional parameters
        parameters.put("Language", "Ar");
        parameters.put("ThemeID", "theme1");
        // if this url is configured for the merchant it's not required
        parameters.put("ResponseBackURL", stsResponseBackUrl);
        parameters.put("Version", "1.0");
        // Create an Ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(stsSecretKey);
        for (String treeMapKey : parameters.keySet()) {
            orderedString.append(parameters.get(treeMapKey));
        }

        log.info("orderdString: " + orderedString);
        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        // Post form
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
        model.addAttribute("SecureHash", secureHash);
        model.addAttribute("RedirectURL", stsPostFormUrl);
        return "submitPayment";
    }

    public RedirectView processPaymentNotification (HttpServletRequest request, HttpServletResponse response) throws IOException {
        Enumeration<String> parameterNames = request.getParameterNames();
        // store all response Parameters to generate Response Secure Hash
        // and get Parameters to use it later in your Code
        Map<String, String> responseParameters = new TreeMap<String, String>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            responseParameters.put(paramName, paramValue);
        }

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(getResponseOrderdString(responseParameters)).getBytes());
        log.info("-->generatedsecureHash is: " + generatedsecureHash);

        // get the received secure hash from result map
        String receivedSecurehash = responseParameters.get("Response.SecureHash");
        log.info("--> receivedSecurehash is: " + receivedSecurehash);

        String transactionId = responseParameters.get("Response.TransactionID");
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new PaymentGatewayException("STS notification, Payment not found");
        }
        Invoice invoice = payment.getInvoice();

        // Build PaymentStatusResponseDTO and call paymentService.updateCreditCardPaymentAndSendEvent
        PaymentStatusResponseDTO.PaymentStatusResponseDTOBuilder paymentStatusResp = PaymentStatusResponseDTO.builder()
            .code(responseParameters.get("Response.StatusCode"))
            .billNumber(invoice.getAccountId().toString())
            .transactionId(transactionId).description(responseParameters.get("Response.GatewayStatusDescription"))
            .cardNumber(responseParameters.get("Response.CardNumber")).cardExpiryDate(responseParameters.get("Response.CardExpiryDate"))
            .amount(responseParameters.get("Response.Amount"));
        if (!receivedSecurehash.equals(generatedsecureHash)) {
            // IF they are not equal then the response shall not be accepted
            log.error("Received Secure Hash does not Equal Generated Secure hash");
        } else {
            // Complete the Action get other parameters from result map and do your processes
            // Please refer to The Integration Manual to see the List of The Received Parameters
            log.info("Status is: {}", responseParameters.get("Response.StatusCode"));
            paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResp.build(), payment);
        }
        // ToDo replace by client redirectUrl
        // response.sendRedirect("http://10.60.75.90:8081/#/customer/test_cc?QBN=7000052830");

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://www.google.com");
        return redirectView;

    }

    public String getResponseOrderdString(Map<String, String> responseParameters) throws UnsupportedEncodingException {
        // Now that we have the map, order it to generate secure hash and compare it with the received one
        StringBuilder responseOrderdString = new StringBuilder();

        responseOrderdString.append(stsSecretKey);
        for (String treeMapKey : responseParameters.keySet()) {
            log.info("--Param key--- {} : {}", treeMapKey, responseParameters.get(treeMapKey));
            switch (treeMapKey) {
                case "Response.SecureHash":
                    log.info("ignoring Response.SecureHash");
                    break;
                case "Response.GatewayStatusDescription":
                    log.info("***case Response.GatewayStatusDescription");
                    String gatewayStatusDescription = URLEncoder.encode(responseParameters.get("Response.GatewayStatusDescription"), "UTF-8");
                    log.info("-->After encoding: {}", gatewayStatusDescription);
                    responseOrderdString.append(gatewayStatusDescription);
                    break;
                case "Response.StatusDescription" :
                    log.info("***case Response.StatusDescription");
                    String statusDescription = URLEncoder.encode(responseParameters.get("Response.StatusDescription"), "UTF-8");
                    log.info("-->After encoding: {}", statusDescription);
                    // ToDO add language to client DB
                    if (true) {
                        statusDescription = statusDescription.toUpperCase();
                    }
                    log.info("-->After Upper: {}", statusDescription);
                    responseOrderdString.append(statusDescription);
                    break;
                default:
                    responseOrderdString.append(responseParameters.get(treeMapKey));
                    break;
            }

        }
        log.info("Response Ordered String is: " + responseOrderdString.toString());
        return responseOrderdString.toString();
    }


    public static void main(String[] args) throws UnsupportedEncodingException {

        /*var generatedsecureHash = "";
        bytes = Encoding.UTF8.GetBytes(responseOrderdstring.ToString());
        sha256 = SHA256.Create();
        hash = sha256.ComputeHash(bytes);
        foreach (byte x in hash)
        {
            generatedsecureHash += string.Format("{0:x2}", x);
        }*/

        // simulte resp
        Map<String, String> map = new TreeMap<>();
        map.put("Response.Amount", "2000");
        map.put("Response.ApprovalCode", "");
        map.put("Response.CardExpiryDate", "2201");
        map.put("Response.CardHolderName", "test");
        map.put("Response.CardNumber", "401200******1112");
        map.put("Response.CurrencyISOCode", "682");
        map.put("Response.GatewayName", "ANBPaymentGateway");
        map.put("Response.GatewayStatusCode", "0000");
        map.put("Response.GatewayStatusDescription", "Payment processed successfully.");
        map.put("Response.MerchantID", "010000085");
        map.put("Response.MessageID", "1");
        map.put("Response.RRN", "202002241052396110000000000");
        map.put("Response.SecureHash", "b5bbac4706119610382ba5b5e5e630a3345898da31b41527bbbd79b17d238315");
        map.put("Response.StatusCode", "00000");
        map.put("Response.StatusDescription", "تمت الحركة بنجاح");
        map.put("Response.TransactionID", "1582530735527");

        CreditCardPaymentService paymentserv = new CreditCardPaymentService();
        String resp = paymentserv.getResponseOrderdString(map);


        String generatedsecureHash = new String(DigestUtils.sha256Hex(resp.toString()).getBytes());
        //byte[] bytes = DigestUtils.sha256(responseOrderdString.toString().getBytes(StandardCharsets.UTF_8));
        //String generatedsecureHash = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("generatedsecureHash: "+ generatedsecureHash);
        // must be ade8c0a20b1742c3a49e8583adb89de5e06ae4aac551822b40b9e5131a3d3c52


        System.out.println(java.net.URLEncoder.encode("Payment processed successfully.", "UTF-8"));
        System.out.println(URLEncoder.encode("Payment processed successfully.", "UTF-8"));

    }

}
