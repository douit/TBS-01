package sa.tamkeentech.tbs.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.service.PayFortPaymentService;
import sa.tamkeentech.tbs.service.STSPaymentService;
import sa.tamkeentech.tbs.service.dto.PayFortOperationDTO;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing {@link Payment}.
 */
@Controller
public class PaymentCreditCardResource {

    private final Logger log = LoggerFactory.getLogger(PaymentCreditCardResource.class);


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final STSPaymentService sTSPaymentService;
    private final PayFortPaymentService payFortPaymentService;

    public PaymentCreditCardResource(STSPaymentService sTSPaymentService, PayFortPaymentService payFortPaymentService) {
        this.sTSPaymentService = sTSPaymentService;
        this.payFortPaymentService = payFortPaymentService;
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
        return sTSPaymentService.initPayment(model, transactionId);
    }

    @PostMapping("/billing/payments/credit-card/notification")
    @ResponseBody
    public void updatePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get All Request Parameters
        log.info("-----got payment notification");
        sTSPaymentService.processPaymentNotification(request, response);
    }

    // ToDo Tmp check only called by Job
    @GetMapping("/billing/check-payment-tmp/{transactionID}")
    @ResponseBody
    public PaymentStatusResponseDTO checkPaymentStatus(@PathVariable String transactionID) {
        return sTSPaymentService.checkOffilnePaymentStatus(transactionID);
    }

    /* Payfort integration */
    /*@GetMapping("/api/payments/payfort-initiate/{invoiceNumber}")
    // @GetMapping("/billing/payfort-signature/{invoiceNumber}")
    @ResponseBody
    public PayFortOperationDTO initPayfortPayment(@PathVariable Long invoiceNumber) throws UnsupportedEncodingException {
        return payFortPaymentService.initPayment(invoiceNumber);
    }*/

    @GetMapping("/billing/payments/payfort-processing")
    @ResponseBody
    public void processPayment(Model model
        , @RequestParam Map<String,Object> params, HttpServletRequest request, HttpServletResponse response) {
        payFortPaymentService.proceedPaymentOperation(params, request, response);
    }

    // THIS IS NOTIFICATION AFTER REDIRECTION TO CUSTOMER
    /*@GetMapping("/billing/payments/payfort/notification")
    @ResponseBody
    public void updatePayment(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam Map<String, Object> params) throws IOException {
        // get All Request Parameters
        log.info("-----got payfort payment notification");
        payFortPaymentService.processPaymentNotification(request, response, params);
    }*/

    // This is post notification in case of connexion issue from payfort directly
    @PostMapping(value= "/billing/payments/payfort/correction", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void paymentCorrectionFromPayfort(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam  Map<String, Object> formData){
        log.info("-----got payment correction {}", formData);
        for (Map.Entry<String, Object> entry: formData.entrySet()) {
            log.info("key {} val {}", entry.getKey(), entry.getValue());
        }
        payFortPaymentService.processPaymentNotification(request, response, formData);
    }

    /*Payfort iframe*/
    @GetMapping("/billing/payments/iframe/{invoiceNumber}")
    public String initPayfortIframe(@PathVariable Long invoiceNumber, Model model, @RequestParam Map<String,String> params) throws UnsupportedEncodingException {
        /*if (params.keySet() == null || !params.keySet().contains(Constants.TRANSACTION_IDENTIFIER_BASE_64)) {
            throw new TbsRunTimeException("Missing parameters");
        }
        String transactionId = new String(Base64.getDecoder().decode(params.get(Constants.TRANSACTION_IDENTIFIER_BASE_64)));*/
        return payFortPaymentService.initIframe(model, invoiceNumber);
    }

}
