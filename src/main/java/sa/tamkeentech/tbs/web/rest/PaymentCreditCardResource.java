package sa.tamkeentech.tbs.web.rest;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.service.CreditCardPaymentService;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * REST controller for managing {@link Payment}.
 */
@Controller
public class PaymentCreditCardResource {

    private final Logger log = LoggerFactory.getLogger(PaymentCreditCardResource.class);


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CreditCardPaymentService creditCardPaymentService;

    public PaymentCreditCardResource(CreditCardPaymentService creditCardPaymentService) {
        this.creditCardPaymentService = creditCardPaymentService;
    }


    @GetMapping("/billing/payments/credit-card")
    public String initCC(Model model) {
        return creditCardPaymentService.initPayment(model);
    }

    @PostMapping("/billing/payments/credit-card/notification")
    @ResponseBody
    public void updatePayment(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException {
        // get All Request Parameters
        log.info("-----got payment notification");

        Enumeration<String> parameterNames = request.getParameterNames();
        // store all response Parameters to generate Response Secure Hash
        // and get Parameters to use it later in your Code
        Map<String, String> responseParameters = new TreeMap<String, String>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            responseParameters.put(paramName, paramValue);
    }

        String responseOrderdString = creditCardPaymentService.getResponseOrderdString(responseParameters);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(responseOrderdString.toString()).getBytes());
        log.info("-->generatedsecureHash is: " + generatedsecureHash);

        // get the received secure hash from result map
        String receivedSecurehash = responseParameters.get("Response.SecureHash");
        log.info("--> receivedSecurehash is: " + receivedSecurehash);

        if (!receivedSecurehash.equals(generatedsecureHash)) {
            // IF they are not equal then the response shall not be accepted
            log.info("Received Secure Hash does not Equal Generated Secure hash");
        } else {
            // Complete the Action get other parameters from result map and do
            // your processes
            // Please refer to The Integration Manual to see the List of The
            // Received Parameters
            String status = responseParameters.get("Response.StatusCode");
            log.info("Status is: " + status);
            httpServletResponse.sendRedirect("http://10.60.75.90:8081/#/customer/test_cc?QBN=7000052830");
        }
    }

    @GetMapping("/check-payment/{transactionID}")
    public String checkPaymentStatus(@PathVariable String transactionID) throws IOException {
        return creditCardPaymentService.checkPaymentStatus(transactionID);
    }
}
