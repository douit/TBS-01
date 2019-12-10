package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.NotificationStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link Payment}.
 */
@Service
@Transactional
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final InvoiceRepository invoiceRepository;

    private final PaymentMethodService paymentMethodService;

    private final ObjectMapper objectMapper;


    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    @Value("${tbs.payment.sadad-account-prefix}")
    private String sadadAccountPrefix;

    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;

    @Value("${tbs.payment.credit-card-url}")
    private String creditCardUrl;

    @Value("${tbs.payment.credit-card-biller-code}")
    private String billerCode;


    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
        this.objectMapper = objectMapper;
    }

    /**
     * Create new credit card payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public PaymentDTO createNewPayment(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        // Payment payment = paymentMapper.toEntity(paymentDTO);
        // Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(paymentDTO.getPaymentMethod().getCode());
        // payment.setPaymentMethod(paymentMethod.get());

        Optional<Invoice> invoice = invoiceRepository.findById(paymentDTO.getInvoiceId());
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Bill does not exist");
        }

        // call payment gateway
        BigDecimal roundedAmount = invoice.get().getAmount().setScale(2, RoundingMode.HALF_UP);
        String appCode = invoice.get().getClient().getPaymentKeyApp();
        PaymentResponseDTO paymentResponseDTO = creditCardCall(invoice.get().getId(), appCode, roundedAmount.multiply(new BigDecimal("100")));

        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(paymentDTO.getPaymentMethod().getCode());
        Payment payment = paymentMapper.toEntity(paymentDTO);
        // Payment payment = Payment.builder().build();
        payment.setPaymentMethod(paymentMethod.get());
        payment.setInvoice(invoice.get());
        payment.setAmount(invoice.get().getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        if (paymentResponseDTO != null) {
            payment.setTransactionId(paymentResponseDTO.getTransactionId());
        }
        payment = paymentRepository.save(payment);

        if (paymentResponseDTO == null || paymentResponseDTO.getTransactionId() == null || StringUtils.isEmpty(paymentResponseDTO.getUrl())) {
            throw new TbsRunTimeException("Payment gateway issue");
        }

        PaymentDTO result = paymentMapper.toDto(payment);
        result.setRedirectUrl(paymentResponseDTO.getUrl());
        return result;
    }

    /**
     *  Update Credit card payment status
     *
     * @param paymentStatusResponseDTO
     * @return
     */
    public PaymentDTO updateCreditCardPayment(PaymentStatusResponseDTO paymentStatusResponseDTO) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
        Payment payment = paymentRepository.findByTransactionId(paymentStatusResponseDTO.getTransactionId());
        Invoice invoice = payment.getInvoice();
        if (Constants.CC_PAYMENT_SUCCESS_CODE.equalsIgnoreCase(paymentStatusResponseDTO.getCode().toString())) {
            payment.setStatus(PaymentStatus.PAID);
            invoice.setPaymentStatus(PaymentStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.UNPAID);
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        paymentRepository.save(payment);
        PaymentDTO result = paymentMapper.toDto(payment);
        return result;
    }


    /**
     * Save a payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentDTO save(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    /**
     * Get all the payments.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> findAll() {
        log.debug("Request to get all Payments");
        return paymentRepository.findAll().stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one payment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findOne(Long id) {
        log.debug("Request to get Payment : {}", id);
        return paymentRepository.findById(id)
            .map(paymentMapper::toDto);
    }

    /**
     * Delete the payment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Payment : {}", id);
        paymentRepository.deleteById(id);
    }


    public int sadadCall(Long sadadBillId, String sadadAccount , BigDecimal amount) throws IOException, JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrl);
        post.setHeader("Content-Type", "application/json");
        //JSONObject accountInfo = new JSONObject();

        JSONObject billInfo = new JSONObject();
        JSONObject billInfoContent = new JSONObject();
        billInfoContent.put("billNumber", sadadBillId ); // autoincrement
        billInfoContent.put("billAccount", sadadAccount); // Unique 15 digits
        billInfoContent.put("amount",amount);
        Calendar c1 = Calendar.getInstance();
        // c.add(Calendar.HOUR, 3);
        // ToDO delete +35 work arround !!!!
        c1.add(Calendar.MINUTE, 35);
        String dueDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c1.getTime());
        billInfoContent.put("duedate", dueDate);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 2);
        String expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        billInfoContent.put("expirydate",expiryDate);
        billInfoContent.put("billStatus","BillNew");
        // applicationId 0 for test
        billInfoContent.put("applicationId",sadadApplicationId);
        billInfo.put("BillInfo", billInfoContent);
        String jsonStr = billInfo.toString();
        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response;
        response = client.execute(post);
       /* if (response.getStatusLine().getStatusCode() == 200){
            return true ;
        }
            return false;*/
        log.debug("----Sadad request : {}", jsonStr);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response content : {}", response.getEntity().getContent());
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }

        return response.getStatusLine().getStatusCode();
    }

    public PaymentResponseDTO creditCardCall( Long invoiceId , String appCode, BigDecimal amount) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(creditCardUrl);
        post.setHeader("Content-Type", "application/json");
        JSONObject billInfoContent = new JSONObject();
        PaymentResponseDTO paymentResponseDTO = null;
        try {
            billInfoContent.put("BillNumber", invoiceId);
            billInfoContent.put("Amount",amount);
            billInfoContent.put("BillerCode",billerCode);
            billInfoContent.put("AppCode",appCode);
            String jsonStr = billInfoContent.toString();
            post.setEntity(new StringEntity(jsonStr));
            HttpResponse response = client.execute(post);
            paymentResponseDTO = objectMapper.readValue(response.getEntity().getContent(), PaymentResponseDTO.class);
        } catch (JSONException | IOException e) {
            log.error("Payment gateway issue: {}", e.getCause());
        }
        log.info("************** response from credit Card ************ : " + paymentResponseDTO);
        return paymentResponseDTO;
    }


    @Transactional
    public ResponseEntity<NotifiRespDTO> sendPaymentNotification(NotifiReqDTO req, String apiKey, String apiSecret) {
        log.debug("----Sadad Notification : {}", req);
        // Optional<Invoice> invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount())-7000000065l);
        Invoice invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount())).get();
        NotifiRespDTO resp = NotifiRespDTO.builder().statusId(1).build();
        for (Payment payment : invoice.getPayments()) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                log.warn("Payment already received, Exit without updating Client app");
                return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
            }
        }

        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(Constants.SADAD);
        Payment payment = Payment.builder()
            .invoice(invoice)
            .status(PaymentStatus.PAID)
            .amount(new BigDecimal(req.getAmount()))
            .paymentMethod(paymentMethod.get())
            //.expirationDate()
            .build();
        paymentRepository.save(payment);

        if (payment.getId() != null) {
            log.info("Successful TBS update bill: {}", req.getBillAccount());
            RestTemplate rt1 = new RestTemplate();
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map1= new LinkedMultiValueMap<String, String>();
            map1.add("grant_type", "client_credentials");
            map1.add("client_id", "tamkeen-billing-system");
            //map1.add("client_secret", "06f4c17f-5c4a-492a-9a8e-a10eafec66c6"); // staging
            map1.add("client_secret", "076a2d1c-15c6-4abf-80a7-0b181f18d617"); // production
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> request1 = new org.springframework.http.HttpEntity<MultiValueMap<String, String>>(map1, headers1);
            //uri = "https://sso.tamkeen.land/auth/realms/tamkeen/protocol/openid-connect/token"; // staging
            String uri = "https://accounts.wahid.sa/auth/realms/wahid/protocol/openid-connect/token"; // production
            ResponseEntity<TokenResponseDTO> response1 = rt1.postForEntity( uri, request1 , TokenResponseDTO.class );
            // log.info("DVS Token" +  response1.getBody().getAccess_token());
            RestTemplate restTemplate = new RestTemplate();
            String ResourceUrl = "http://10.60.71.16:8880/dvs/?billnumber=";
            ResponseEntity<NotifiRespDTO> response2= restTemplate.getForEntity(ResourceUrl + req.getBillAccount() + "&paymentdate=" + req.getPaymentDate() + "&token=" + response1.getBody().getAccess_token() , NotifiRespDTO.class);
            log.info("Succuss DVS update" + response2.getBody().getStatusId());
            // NotifiResp resp = (NotifiResp)response2.getBody(); // only for testing
            invoice.setPaymentStatus(PaymentStatus.PAID);
            invoice.setNotificationStatus(NotificationStatus.PAYMENT_NOTIFICATION_SUCCESS);
            invoiceRepository.save(invoice);
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
        } else {
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DataTablesOutput<PaymentDTO> get(DataTablesInput input) {
        return paymentMapper.toDto(paymentRepository.findAll(input));
    }

}
