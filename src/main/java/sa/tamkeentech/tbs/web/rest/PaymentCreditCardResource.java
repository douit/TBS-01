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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.service.CreditCardPaymentService;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

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
    public String initCC(Model model, @RequestParam Map<String,String> params) {
        // use https://www.codepunker.com/tools/string-converter  --> base64 deccode
        /*{
         "base64": "dHJhbnNhY3Rpb25JZGVudGlmaWVy",
         "url": "transactionIdentifier"
        }*/
        if (params.keySet() == null || !params.keySet().contains(Constants.TRANSACTION_IDENTIFIER_BASE_64)) {
            throw new TbsRunTimeException("Missing parameters");
        }
        String transactionId = new String(Base64.getDecoder().decode(params.get(Constants.TRANSACTION_IDENTIFIER_BASE_64)));
        return creditCardPaymentService.initPayment(model, transactionId);
    }

    @PostMapping("/billing/payments/credit-card/notification")
    @ResponseBody
    public RedirectView updatePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get All Request Parameters
        log.info("-----got payment notification");
        return creditCardPaymentService.processPaymentNotification(request, response);
    }


    // testing
    /*@GetMapping("/billing/payments/credit-card/notification")
    @ResponseBody
    public void redirectTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get All Request Parameters
        log.info("-----");
        *//*RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://www.google.com");
        return redirectView;*//*
        test(response);
    }

    private RedirectView test(HttpServletResponse response) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("https://www.google.com");
        return redirectView;
    }*/

    @GetMapping("/check-payment/{transactionID}")
    public String checkPaymentStatus(@PathVariable String transactionID) throws IOException {
        return creditCardPaymentService.checkPaymentStatus(transactionID);
    }
}
