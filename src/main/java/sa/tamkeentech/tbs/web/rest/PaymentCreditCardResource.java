package sa.tamkeentech.tbs.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.enumeration.PaymentProvider;
import sa.tamkeentech.tbs.service.PayFortPaymentService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.STSPaymentService;
import sa.tamkeentech.tbs.service.dto.ApplePayTokenAuthorizeDTO;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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

    private final STSPaymentService sTSPaymentService;
    private final PayFortPaymentService payFortPaymentService;
    @Inject
    @Lazy
    private PaymentService paymentService;

    public PaymentCreditCardResource(STSPaymentService sTSPaymentService, PayFortPaymentService payFortPaymentService) {
        this.sTSPaymentService = sTSPaymentService;
        this.payFortPaymentService = payFortPaymentService;
    }


    /**
     * Load payment form for STS / Payfort
     * @param model
     * @param params
     * @return
     */
    @GetMapping("/billing/payments/credit-card")
    public String initCC(Model model, @RequestParam Map<String,String> params) {
        // use https://www.codepunker.com/tools/string-converter  --> base64 deccode
        /*{
         "base64": "dHJhbnNhY3Rpb25JZGVudGlmaWVy",
         "url": "transactionIdentifier"
        }*/
        if (params.keySet() == null || !params.keySet().contains(Constants.TRANSACTION_IDENTIFIER_BASE_64)) {
            // ToDo change to error page
            throw new TbsRunTimeException("Missing parameters");
        }
        String transactionId = new String(Base64.getDecoder().decode(params.get(Constants.TRANSACTION_IDENTIFIER_BASE_64)));
        String lang = (StringUtils.isNotEmpty(params.get(Constants.REQUEST_PARAM_LANGUAGE)))
            ? params.get(Constants.REQUEST_PARAM_LANGUAGE) : Constants.DEFAULT_HEADER_LANGUAGE;
        return paymentService.initPayment(model, transactionId, lang);
    }


    @PostMapping("/billing/payments/credit-card/notification")
    @ResponseBody
    public void updatePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get All Request Parameters
        log.info("-----got STS payment notification");
        sTSPaymentService.processPaymentNotification(request, response);
    }

    // ToDo Tmp check only called by Job
    @GetMapping("/billing/check-payment-tmp/{provider}/{transactionID}")
    @ResponseBody
    public PaymentStatusResponseDTO checkPaymentStatus(@PathVariable String provider, @PathVariable String transactionID) {
        if (PaymentProvider.STS.name().equals(provider))
            return sTSPaymentService.checkOffilnePaymentStatus(transactionID);
        else
            return payFortPaymentService.checkOffilnePaymentStatus(transactionID);
    }


    @PostMapping(value= "/billing/payments/payfort-processing", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public void processPayment(Model model
        , @RequestParam Map<String,Object> params, HttpServletRequest request, HttpServletResponse response) {
        payFortPaymentService.proceedPaymentOperation(params, request, response);
    }


    @GetMapping("/billing/payments/payfort-processing")
    @ResponseBody
    public void processPaymentGet(Model model
        , @RequestParam Map<String,Object> params, HttpServletRequest request, HttpServletResponse response) {
        payFortPaymentService.proceedPaymentOperation(params, request, response);
    }

    // This is post notification in case of connexion issue from payfort directly
    @PostMapping(value= "/billing/payments/payfort/correction", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void paymentCorrectionFromPayfort(HttpServletRequest request, HttpServletResponse response,
                                             @RequestParam  Map<String, Object> formData){
        log.info("-----got payfort payment correction {}", formData);
        payFortPaymentService.processPaymentNotification(request, response, formData, true);
    }

    @PostMapping("/billing/payments/apple-session")
    @ResponseBody
    public String generateAppleSession(@RequestBody Map<String, String> payload) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, JSONException {
        String validationURL = payload.get("validationUrl");
        String transactionId = payload.get("transactionId");
        String session =  payFortPaymentService.generateSession(validationURL, transactionId);
        log.debug("---Apple pay generate session: {}", session);
        return session;
    }


    @PostMapping("/billing/payments/apple-authorize")
    @ResponseBody
    public String authorizeApplePayment(@RequestBody ApplePayTokenAuthorizeDTO token, HttpServletRequest request,
                                        HttpServletResponse response) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException, JSONException {
        log.debug("---Apple pay token to process payment: {}", token);
        return payFortPaymentService.proceedApplePurchaseOperation(token, request, response);
    }

}
