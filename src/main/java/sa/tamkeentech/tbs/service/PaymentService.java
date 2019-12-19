package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.checkerframework.checker.units.qual.Current;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.ClientMapper;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
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
    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final EventPublisherService eventPublisherService;

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


    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, ObjectMapper objectMapper, EventPublisherService eventPublisherService, ClientService clientService,ClientMapper clientMapper ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
        this.objectMapper = objectMapper;
        this.eventPublisherService = eventPublisherService;
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    /**
     * Create new credit card payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public PaymentDTO prepareCreditCardPayment(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Optional<Invoice> invoice = invoiceRepository.findByAccountId(paymentDTO.getInvoiceId());
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Bill does not exist");
        }

        TBSEventReqDTO<PaymentDTO> reqNotification = TBSEventReqDTO.<PaymentDTO>builder().principalId(invoice.get().getCustomer().getIdentity())
            .referenceId(invoice.get().getAccountId().toString()).req(paymentDTO).build();

        return eventPublisherService.initiateCreditCardPaymentEvent(reqNotification, invoice).getResp();
    }

    public PaymentDTO initiateCreditCardPayment(PaymentDTO req, Optional<Invoice> invoice) {
        // call payment gateway
        BigDecimal roundedAmount = invoice.get().getAmount().setScale(2, RoundingMode.HALF_UP);
        String appCode = invoice.get().getClient().getPaymentKeyApp();
        PaymentResponseDTO paymentResponseDTO = null;
        try {
            paymentResponseDTO = sendEventAndCreditCardCall(invoice, appCode, roundedAmount.multiply(new BigDecimal("100")));
        } catch (JSONException | IOException e) {
            throw new TbsRunTimeException("Payment gateway issue: "+ e.getCause());
        }

        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(req.getPaymentMethod().getCode());
        Payment payment = paymentMapper.toEntity(req);
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
    public PaymentDTO updateCreditCardPaymentAndSendEvent(PaymentStatusResponseDTO paymentStatusResponseDTO) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
        Payment payment = paymentRepository.findByTransactionId(paymentStatusResponseDTO.getTransactionId());
        Invoice invoice = payment.getInvoice();

        TBSEventReqDTO<PaymentStatusResponseDTO> reqNotification = TBSEventReqDTO.<PaymentStatusResponseDTO>builder().principalId(invoice.getCustomer().getIdentity())
            .referenceId(invoice.getAccountId().toString()).req(paymentStatusResponseDTO).build();

        return eventPublisherService.creditCardNotificationEvent(reqNotification, payment, invoice).getResp();
    }

    public PaymentDTO updateCreditCardPayment(PaymentStatusResponseDTO paymentStatusResponseDTO, Payment payment, Invoice invoice) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
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


    public int sendEventAndCallSadad(Long sadadBillId, String sadadAccount , BigDecimal amount, String principal) throws IOException, JSONException {

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
        TBSEventReqDTO<String> req = TBSEventReqDTO.<String>builder()
            .principalId(principal).referenceId(sadadAccount).req(billInfo.toString()).build();
        return eventPublisherService.callSadadEvent(req).getResp();

    }

    public Integer callSadad(String req) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrl);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(req));
        HttpResponse response;
        response = client.execute(post);

        log.debug("----Sadad request : {}", req);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }

        return response.getStatusLine().getStatusCode();
    }

    public PaymentResponseDTO sendEventAndCreditCardCall(Optional<Invoice> invoice , String appCode, BigDecimal amount) throws JSONException, IOException {

        JSONObject billInfoContent = new JSONObject();
        billInfoContent.put("BillNumber", invoice.get().getAccountId());
        billInfoContent.put("Amount",amount);
        billInfoContent.put("BillerCode",billerCode);
        billInfoContent.put("AppCode",appCode);

        TBSEventReqDTO<String> req = TBSEventReqDTO.<String>builder()
            .principalId(invoice.get().getCustomer().getIdentity()).referenceId(invoice.get().getAccountId().toString()).req(billInfoContent.toString()).build();
        PaymentResponseDTO resp = eventPublisherService.callCreditCardInitiateEvent(req).getResp();
        return resp;

    }

    public PaymentResponseDTO callCreditCard(String jsonStr) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(creditCardUrl);
        post.setHeader("Content-Type", "application/json");
        PaymentResponseDTO paymentResponseDTO = null;

        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response = client.execute(post);
        paymentResponseDTO = objectMapper.readValue(response.getEntity().getContent(), PaymentResponseDTO.class);

        log.info("************** response from credit Card ************ : " + paymentResponseDTO);
        return paymentResponseDTO;
    }


    // ToDo divide into 2 parts: receive from connector | notify client
    @Transactional
    public ResponseEntity<NotifiRespDTO> sendEventAndPaymentNotification(NotifiReqDTO req, String apiKey, String apiSecret) {
        log.debug("----Sadad Notification : {}", req);
        Invoice invoice = invoiceRepository.findByAccountId(Long.parseLong(req.getBillAccount())).get();

        TBSEventReqDTO<NotifiReqDTO> reqNotification = TBSEventReqDTO.<NotifiReqDTO>builder()
            .principalId(invoice.getCustomer().getIdentity()).referenceId(invoice.getAccountId().toString())
            .req(req).build();
        NotifiRespDTO resp = eventPublisherService.sendPaymentNotification(reqNotification, invoice).getResp();

        return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
    }

    public NotifiRespDTO sendPaymentNotification(NotifiReqDTO reqNotification, Invoice invoice) {
        NotifiRespDTO resp = NotifiRespDTO.builder().statusId(1).build();
        for (Payment payment : invoice.getPayments()) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                log.warn("Payment already received, Exit without updating Client app");
                return resp;
            }
        }
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(Constants.SADAD);
        Payment payment = Payment.builder()
            .invoice(invoice)
            .status(PaymentStatus.PAID)
            .amount(new BigDecimal(reqNotification.getAmount()))
            .paymentMethod(paymentMethod.get())
            .bankId(reqNotification.getBankId())
            .transactionId(reqNotification.getTransactionPaymentId())
            //.expirationDate()
            .build();
        paymentRepository.save(payment);

        log.info("Successful TBS update bill: {}", reqNotification.getBillAccount());

        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoiceRepository.save(invoice);
        sendPaymentNotificationToClint(clientMapper.toDto(invoice.getClient()) ,invoice.getAccountId(),payment);
        return resp;
    }

    @Scheduled(cron = "0  5  *  *  * ?")
    public void sendPaymentNotificationToClintJob(){

        List<ClientDTO> clients =  clientService.findAll();
        for(ClientDTO clientDTO : clients){
            List<Optional<Invoice>> invoices = invoiceRepository.findByStatusAndClient(InvoiceStatus.WITTING,clientMapper.toEntity(clientDTO));
            for (Optional<Invoice> invoice : invoices) {
                for (Payment payment : invoice.get().getPayments()) {
                    if (payment.getStatus() == PaymentStatus.PAID) {
                        sendPaymentNotificationToClint(clientDTO,invoice.get().getAccountId(),payment);
                    }
                }
            }
        }

    }

    public void sendPaymentNotificationToClint(ClientDTO client , Long accountId , Payment payment){
        Date currentDate = new Date();
        Date tokenModifiedDate = new Date();
        if (client.getTokenModifiedDate() != null) {
            tokenModifiedDate = Date.from(client.getTokenModifiedDate().toInstant());

        }
        long diff = currentDate.getTime() - tokenModifiedDate.getTime();
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);


        String token = null;
        if (diffDays >= 1 || diffHours >= 1 || diffMinutes > 59 || client.getClientToken() == null ) {
            RestTemplate rt1 = new RestTemplate();
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map1 = new LinkedMultiValueMap<String, String>();
            map1.add("grant_type", "client_credentials");
            map1.add("client_id", "tamkeen-billing-system");
            //map1.add("client_secret", "06f4c17f-5c4a-492a-9a8e-a10eafec66c6"); // staging
            map1.add("client_secret", "076a2d1c-15c6-4abf-80a7-0b181f18d617"); // production
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> request1 = new org.springframework.http.HttpEntity<MultiValueMap<String, String>>(map1, headers1);
            //  String uri = "https://sso.tamkeen.land/auth/realms/tamkeen/protocol/openid-connect/token"; // staging
            String uri = "https://accounts.wahid.sa/auth/realms/wahid/protocol/openid-connect/token"; // production
            ResponseEntity<TokenResponseDTO> response1 = rt1.postForEntity(uri, request1, TokenResponseDTO.class);
            token = response1.getBody().getAccess_token();
            client.setClientToken(token);
            client.setTokenModifiedDate(currentDate.toInstant().atZone(ZoneId.systemDefault()));
            clientService.save(client);

            // log.info("DVS Token" + token);
        } else {
            token = client.getClientToken();
        }


        RestTemplate restTemplate = new RestTemplate();
        String pattern = " dd/MM/yyyy hh:mm:ss a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String paymentDate = simpleDateFormat.format(Date.from(payment.getCreatedDate().toInstant()));
        // String ResourceUrl = "http://10.60.71.16:8880/dvs/?billnumber=";
        // ResponseEntity<NotifiRespDTO> response2= restTemplate.getForEntity(ResourceUrl + accountId.toString() + "&paymentdate="
        //   +  paymentDate + "&token=" + token , NotifiRespDTO.class);
        //log.info("Succuss DVS update" + response2.getBody().getStatusId());
        // NotifiResp resp = (NotifiResp)response2.getBody(); // only for testing
//                    if(response2.getBody().getStatusId() == 200){
//                        invoice.setStatus(InvoiceStatus.CLIENT_NOTIFIED);
//                    }



    }
    public DataTablesOutput<PaymentDTO> get(DataTablesInput input) {
        return paymentMapper.toDto(paymentRepository.findAll(input));
    }

}
