package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentProvider;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.*;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.ClientMapper;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;
import sa.tamkeentech.tbs.service.mapper.PaymentMethodMapper;
import sa.tamkeentech.tbs.service.util.CommonUtils;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link Payment}.
 */
@Service
// @Transactional
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final InvoiceRepository invoiceRepository;
    private final BankRepository bankRepository;
    private final BinRepository binRepository;

    private final PaymentMethodService paymentMethodService;

    private final ObjectMapper objectMapper;
    private final ClientService clientService;
    private final ClientMapper clientMapper;
    private final EventPublisherService eventPublisherService;
    private final PersistenceAuditEventRepository persistenceAuditEventRepository;
    private final PaymentMethodMapper paymentMethodMapper;
    private final ClientRepository clientRepository;

    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;

    /*@Value("${tbs.payment.credit-card-url}")
    private String creditCardUrl;*/

    @Value("${tbs.payment.credit-card-biller-code}")
    private String billerCode;

    @Value("${tbs.payment.url}")
    private String paymentUrl;

    @Autowired
    private Environment environment;

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private STSPaymentService sTSPaymentService;

    @Autowired
    @Lazy
    private PayFortPaymentService payFortPaymentService;

    @Autowired
    private RestTemplate restTemplate;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, InvoiceRepository invoiceRepository, BankRepository bankRepository, BinRepository binRepository, PaymentMethodService paymentMethodService, ObjectMapper objectMapper, EventPublisherService eventPublisherService, ClientService clientService, ClientMapper clientMapper, PersistenceAuditEventRepository persistenceAuditEventRepository, PaymentMethodMapper paymentMethodMapper, ClientRepository clientRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
        this.bankRepository = bankRepository;
        this.binRepository = binRepository;
        this.paymentMethodService = paymentMethodService;
        this.objectMapper = objectMapper;
        this.eventPublisherService = eventPublisherService;
       this.clientService =clientService;
        this.clientMapper = clientMapper;
        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.paymentMethodMapper = paymentMethodMapper;
        this.clientRepository = clientRepository;
    }


    /**
     * Load form payment according to the provider
     * @param model
     * @param transactionId
     * @return
     */
    @Transactional
    public String initPayment(Model model, String transactionId) {
        log.info("Request to initiate Payment : {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            // ToDo change to error page
            throw new TbsRunTimeException("Payment not found");
        }
        if (payment.getPaymentProvider() == PaymentProvider.PAYFORT) {
            return payFortPaymentService.initPayment(model, payment);
        } else {
            return sTSPaymentService.initPayment(model, payment);
        }
    }

    /**
     * Create new credit card payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    /*@Transactional
    public PaymentDTO prepareCreditCardPayment(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Optional<Invoice> invoice = invoiceRepository.findByAccountId(paymentDTO.getInvoiceId());
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Bill does not exist");
        }

        TBSEventReqDTO<PaymentDTO> reqNotification = TBSEventReqDTO.<PaymentDTO>builder().principalId(invoice.get().getCustomer().getIdentity())
            .referenceId(invoice.get().getAccountId().toString()).req(paymentDTO).build();

        return eventPublisherService.initiateCreditCardPaymentEvent(reqNotification, invoice).getResp();
    }*/

    /**
     *
     * @param req
     * @param invoice
     * @return
     */
    /*@Transactional
    public PaymentDTO initiateCreditCardPayment(PaymentDTO req, Optional<Invoice> invoice) {
        // call payment gateway
        BigDecimal roundedAmount = invoice.get().getAmount().setScale(2, RoundingMode.HALF_UP);
        String appCode = invoice.get().getClient().getPaymentKeyApp();
        PaymentResponseDTO paymentResponseDTO = null;
        try {
            paymentResponseDTO = sendEventAndCreditCardCall(invoice, appCode, roundedAmount.multiply(new BigDecimal("100")));
        } catch (JSONException | IOException e) {
            throw new PaymentGatewayException("Payment gateway issue: "+ e.getCause());
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
            throw new PaymentGatewayException("Payment gateway issue");
        }

        PaymentDTO result = paymentMapper.toDto(payment);
        result.setRedirectUrl(paymentResponseDTO.getUrl());
        return result;
    }*/

    /**
     *  Update Credit card payment status
     *
     * @param paymentStatusResponseDTO
     * @return
     */
    @Transactional
    public PaymentDTO updateCreditCardPaymentAndSendEvent(PaymentStatusResponseDTO paymentStatusResponseDTO, Payment payment) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
        // Payment payment = paymentRepository.findByTransactionId(paymentStatusResponseDTO.getTransactionId());
        Invoice invoice = payment.getInvoice();

        TBSEventReqDTO<PaymentStatusResponseDTO> reqNotification = TBSEventReqDTO.<PaymentStatusResponseDTO>builder().principalId(invoice.getCustomer().getIdentity())
            .referenceId(invoice.getAccountId().toString()).req(paymentStatusResponseDTO).build();

        return eventPublisherService.creditCardNotificationEvent(reqNotification, payment, invoice).getResp();
    }

    @Transactional
    public PaymentDTO updateCreditCardPayment(PaymentStatusResponseDTO paymentStatusResponseDTO, Payment payment, Invoice invoice) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
        if ((Constants.STS_PAYMENT_SUCCESS_CODE.equalsIgnoreCase(paymentStatusResponseDTO.getCode())
        || Constants.PAYFORT_PAYMENT_SUCCESS_CODE.equalsIgnoreCase(paymentStatusResponseDTO.getCode())) && payment.getStatus() == PaymentStatus.CHECKOUT_PAGE) {
            payment.setStatus(PaymentStatus.PAID);
            invoice.setPaymentStatus(PaymentStatus.PAID);
            // in case client does not call check-payment Job will send notification
            invoice.setStatus(InvoiceStatus.WAITING);
        } else if ((Constants.STS_PAYMENT_FAILURE_CODE.equalsIgnoreCase(paymentStatusResponseDTO.getCode())
            || Constants.PAYFORT_PAYMENT_FAILURE_CODE.equalsIgnoreCase(paymentStatusResponseDTO.getCode())) && payment.getStatus() == PaymentStatus.CHECKOUT_PAGE) {
            payment.setStatus(PaymentStatus.UNPAID);
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        if (StringUtils.isNotEmpty(paymentStatusResponseDTO.getCardNumber()) && paymentStatusResponseDTO.getCardNumber().length() > 5) {
            payment.setBankId(findBankCode(paymentStatusResponseDTO.getCardNumber()));
        }
        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        PaymentDTO result = paymentMapper.toDto(payment);
        return result;
    }

    @Transactional
    public PaymentDTO checkPaymentStatus(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new TbsRunTimeException("No payments found");
        }
        // update invoice Status to CLIENT_NOTIFIED
        Invoice invoice = payment.getInvoice();
        invoice.setStatus(InvoiceStatus.CLIENT_NOTIFIED);
        invoiceRepository.save(invoice);

        return PaymentDTO.builder()
            .billNumber(payment.getInvoice().getAccountId().toString())
            .transactionId(payment.getTransactionId())
            .status(payment.getStatus())
            .paymentMethod(paymentMethodMapper.toDto(payment.getPaymentMethod()))
            .amount(payment.getAmount()).build();
    }

    private String findBankCode(String cardNumber) {
        for(Bin bin : binRepository.findAll()){
            if(bin.getBin().toString().substring(0,5).equals(cardNumber.substring(0,5))){
               return bankRepository.findById(bin.getIdBank()).get().getCode();
            }
        }
        return "";
    }


    /**
     * Save a payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
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
     *
     * @param sadadBillId
     * @param sadadAccount
     * @param amount
     * @param principal
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @Transactional
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
        c.add(Calendar.DATE, Constants.INVOICE_EXPIRY_DAYS);
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

    @Transactional
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


    /*@Transactional
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

    }*/

    /*@Transactional
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
    }*/

    // @Transactional
    public ResponseEntity<NotifiRespDTO> sendEventAndPaymentNotification(NotifiReqDTO req, String apiKey, String apiSecret) {
        log.debug("----Sadad Notification : {}", req);
        Invoice invoice = invoiceRepository.findByAccountId(Long.parseLong(req.getBillAccount())).orElse(null);

        String principalId = getCustomerId(invoice.getCustomer());
        TBSEventReqDTO<NotifiReqDTO> reqNotification = TBSEventReqDTO.<NotifiReqDTO>builder()
            .principalId(principalId)
            .referenceId(invoice.getAccountId().toString())
            .req(req).build();
        NotifiRespDTO resp = eventPublisherService.sendPaymentNotification(reqNotification, invoice).getResp();

        return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
    }

    // @Transactional
    public NotifiRespDTO sendSadadPaymentNotification(NotifiReqDTO reqNotification, Invoice invoice) {

        if (invoice == null) {
            return NotifiRespDTO.builder()
                .statusId(0)
                .statusDescription("Invoice does not exist")
                .build();
        } else if (invoice.getAmount().compareTo(new BigDecimal(reqNotification.getAmount())) != 0) {
            return NotifiRespDTO.builder()
                .statusId(0)
                .statusDescription("Wrong amount")
                .build();
        } else {
            Optional<Payment> paymentFromDb = paymentRepository.findFirstByInvoiceAccountIdAndStatus(invoice.getAccountId(), PaymentStatus.PAID);
            if (paymentFromDb.isPresent()) {
                log.warn("Payment already received, Exit without updating Client app");
                return NotifiRespDTO.builder()
                    .statusId(1)
                    .statusDescription("Payment already received, Exit without updating Client app")
                    .build();

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

            if (createSadadPaymentAndUpdateInvoice(reqNotification, invoice, payment)) {
                try {
                    // call sso only in prod, Later limit this to Tahaquq to avoid timeout and Sentry issue
                    if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "staging")
                        || CommonUtils.isProfile(environment, "ahmed")) {
                        sendPaymentNotificationToClient(clientMapper.toDto(invoice.getClient()), invoice.getAccountId(), payment);
                    } else {
                        log.warn("----Not prod/staging env, client notif desabled, Invoice: {}"+ invoice.getAccountId());
                    }
                } catch (Exception e) {
                    log.error("---Payment notification Failed");
                    e.printStackTrace();
                    Sentry.capture(e);
                }
                return NotifiRespDTO.builder()
                    .statusId(1)
                    .build();
            }
        }

        return NotifiRespDTO.builder()
            .statusId(0)
            .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean createSadadPaymentAndUpdateInvoice(NotifiReqDTO reqNotification, Invoice invoice, Payment payment) {
        paymentRepository.save(payment);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setStatus(InvoiceStatus.WAITING);
        invoiceRepository.save(invoice);
        log.info("---Successful TBS update bill: {}", reqNotification.getBillAccount());
        return true;
    }


    @Scheduled(cron = "${tbs.cron.payment-notification-correction}")
    public void sendPaymentNotificationToClientJob() {

        List<ClientDTO> clients =  clientService.findAll();
        for(ClientDTO clientDTO : clients){
            // Add lastModifiedDate before 1 min to avoid sending notif before check-payment
            List<Optional<Invoice>> invoices = invoiceRepository.findByStatusAndClientIdAndLastModifiedDateBefore(InvoiceStatus.WAITING,
                clientDTO.getId(), ZonedDateTime.now().minusMinutes(1));
            log.warn("----Payment notification correction, client {} total {}", clientDTO.getName(), invoices.size());
            for (Optional<Invoice> invoice : invoices) {
                Optional<Payment> payment = paymentRepository.findFirstByInvoiceAccountIdAndStatus(invoice.get().getAccountId(), PaymentStatus.PAID);
                if (payment.isPresent()) {
                    if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "staging")
                        || CommonUtils.isProfile(environment, "ahmed")) {
                        sendPaymentNotificationToClient(clientDTO, invoice.get().getAccountId(), payment.get());
                    } else {
                        log.warn("----Not prod/staging env, Invoice status is waiting and payment found, Invoice: {}"+ invoice.get().getAccountId());
                    }
                } else {
                    log.warn("----Invoice status is waiting but no payment found, Invoice: {}"+ invoice.get().getAccountId());
                }
            }
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void sendPaymentNotificationToClient(ClientDTO client , Long accountId , Payment payment){
        Date currentDate = new Date();
        // tokenModifiedDate means deadline, after that new token must be generated
        Date tokenModifiedDate = new Date();
        if (client.getTokenExpiryDate() != null) {
            tokenModifiedDate = Date.from(client.getTokenExpiryDate().toInstant());
        }
        //ChronoUnit.SECONDS.between(tokenModifiedDate.getTime(), currentDate.getTime());
        long diffInMinutes = currentDate.toInstant().until(tokenModifiedDate.toInstant(), ChronoUnit.MINUTES);
        log.info("---> sendPaymentNotificationToClient token expires after {} min", diffInMinutes);
        String token = null;
        if (diffInMinutes <= 1 || client.getClientToken() == null ) {
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map1 = new LinkedMultiValueMap<String, String>();
            map1.add("grant_type", "client_credentials");
            map1.add("client_id", "tamkeen-billing-system");
            //map1.add("client_secret", "06f4c17f-5c4a-492a-9a8e-a10eafec66c6"); // staging
            map1.add("client_secret", environment.getProperty("tbs.payment.wahid-secret")); // production
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> request1 = new org.springframework.http.HttpEntity<MultiValueMap<String, String>>(map1, headers1);
            //  String uri = "https://sso.tamkeen.land/auth/realms/tamkeen/protocol/openid-connect/token"; // staging
            String uri =  environment.getProperty("tbs.payment.wahid-url"); // production
            ResponseEntity<TokenResponseDTO> response1 = restTemplate.postForEntity(uri, request1, TokenResponseDTO.class);
            token = response1.getBody().getAccess_token();

            Optional<Client> clientEntity = clientRepository.findById(client.getId());
            clientEntity.get().setClientToken(token);
            ZonedDateTime expiryDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).plusSeconds(response1.getBody().getExpires_in());
            clientEntity.get().setTokenExpiryDate(expiryDate);
            clientRepository.save(clientEntity.get());

            // log.info("DVS Token" + token);
        } else {
            token = client.getClientToken();
        }

        String pattern = " dd/MM/yyyy hh:mm:ss a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String paymentDate = simpleDateFormat.format(Date.from(payment.getCreatedDate().toInstant()));
        String resourceUrl = "";
        /*if(Arrays.stream(environment.getActiveProfiles()).anyMatch(
            env -> (env.equalsIgnoreCase("prod")) )) {*/
        if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "staging")) {
        // if (CommonUtils.isProdOrStaging(environment)) {
            resourceUrl += client.getNotificationUrl();
        } else {
            resourceUrl += "https://test";
        }
        // resourceUrl += ("?billnumber=" + accountId.toString() + "&paymentdate=" +  paymentDate + "&token=" + token);
        log.info("----calling Client update: "+ resourceUrl);

        PaymentDTO paymentNotifReqToClientDTO = PaymentDTO.builder()
            .billNumber(accountId.toString())
            .transactionId(payment.getTransactionId())
            .status(PaymentStatus.PAID)
            .paymentMethod(paymentMethodMapper.toDto(payment.getPaymentMethod()))
            // .paymentDate(CommonUtils.getFormattedLocalDate(payment.getLastModifiedDate(), Constants.RIYADH_OFFSET))
            .paymentDate(payment.getLastModifiedDate())
            .amount(payment.getAmount()).build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+token);
        headers.set("Content-Type", "application/json");
        headers.set("client_id", environment.getProperty("tbs.payment.notification-client-id"));
        headers.set("client_secret", environment.getProperty("tbs.payment.notification-client-secret"));

        HttpEntity<PaymentDTO> request = new HttpEntity<>(paymentNotifReqToClientDTO, headers);
        ResponseEntity<PaymentNotifResFromClientDTO> response2= null;
        try {
            response2 = restTemplate.exchange(resourceUrl, HttpMethod.POST, request, PaymentNotifResFromClientDTO.class);
        } catch (HttpServerErrorException e) {
            log.error("Payment notif Response satatus:{}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            e.printStackTrace();
            Sentry.capture(e);
        }

        if (response2 != null && response2.getStatusCode().is2xxSuccessful()/* response2.getBody()!= null && response2.getBody().getStatusId() == 1*/) {
            log.info("----Successful Client update: " + response2.getBody().getStatusId());
            // abaan  ----Successful Client update: 1 can uncomment the above
            Optional<Invoice> invoice = invoiceRepository.findByAccountId(accountId);
            invoice.get().setStatus(InvoiceStatus.CLIENT_NOTIFIED);
            invoiceRepository.save(invoice.get());
        } else {
            log.info("----Issue Client update: {} -- status code: {}", ((response2 != null && response2.getBody()!= null)? response2.getBody().getStatusId(): "Empty body"), (response2 != null)? response2.getStatusCode(): "Null resp");
        }
    }

    @Transactional
    public InvoiceResponseDTO requestNewPayment(String referenceId, String paymentMethodCode) {

        Optional<Invoice> invoice = invoiceRepository.findByAccountId(Long.parseLong(referenceId));
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Invoice does not exist");
        } else if (invoice.get().getPaymentStatus() == PaymentStatus.PAID) {
            throw new TbsRunTimeException("Invoice already paid");
        } else if (invoice.get().getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw new TbsRunTimeException("Invoice already refunded");
        } else if (invoice.get().getStatus() == InvoiceStatus.EXPIRED) {
            throw new TbsRunTimeException("Invoice already expired");
        }
        InvoiceResponseDTO invoiceResponseDTO = InvoiceResponseDTO.builder().statusId(1).shortDesc("success").description("").build();
        invoiceResponseDTO.setBillNumber(invoice.get().getAccountId().toString());
        if (paymentMethodCode.equals(Constants.SADAD)) {
            // check if Sadad already called in event
            Optional<PersistentAuditEvent> event = persistenceAuditEventRepository.findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(Long.parseLong(referenceId), true, Constants.EventType.SADAD_INITIATE.name());
            if (!event.isPresent()) {
                int sadadResult;

                try {
                    String principalId = getCustomerId(invoice.get().getCustomer());
                    sadadResult = sendEventAndCallSadad(invoice.get().getNumber(), invoice.get().getAccountId().toString(), invoice.get().getAmount(), principalId);
                } catch (IOException | JSONException e) {
                    throw new PaymentGatewayException("Sadad issue");
                }
                if (sadadResult != 200) {
                    throw new PaymentGatewayException("Sadad bill creation error");
                }
            }

        } else {
            Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(paymentMethodCode);
            invoiceResponseDTO.setLink(savePaymentAndGetPaymentUrl(invoice.get(), paymentMethod.get()));
            invoiceRepository.save(invoice.get());
        }
        return invoiceResponseDTO;
    }

    public String savePaymentAndGetPaymentUrl(Invoice invoice, PaymentMethod paymentMethod) {
        DateFormat df = new SimpleDateFormat("HHmmss");
        String transactionId = invoice.getAccountId().toString() + df.format(new Timestamp(System.currentTimeMillis()));

        Payment payment = Payment.builder().build();//paymentMapper.toEntity(paymentDTO);
        payment.setPaymentMethod(paymentMethod);
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(transactionId);
        payment.setPaymentProvider(invoice.getClient().getPaymentProvider());

        paymentRepository.save(payment);

        return (paymentUrl + Constants.TRANSACTION_IDENTIFIER_BASE_64 + "=" + Base64.getEncoder().encodeToString(transactionId.getBytes()));
    }

    public DataTablesOutput<PaymentDTO> get(DataTablesInput input) {
        return paymentMapper.toDto(paymentRepository.findAll(input, (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<Long> clientIds = userService.listClientIds(null);
            predicates.add(criteriaBuilder.and(root.get("invoice").get("client").get("id").in(clientIds)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }

    @Transactional(readOnly = true)
    public DataTablesOutput<PaymentDTO> getPaymentStatusByQuerySearch(PaymentSearchRequestDTO paymentSearchRequestDTO) {
        // return itemMapper.toDto(itemRepository.findAll(input));
        return paymentMapper.toDto(paymentRepository.findAll(paymentSearchRequestDTO.getInput(), (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            List<Long> clientIds = userService.listClientIds(paymentSearchRequestDTO.getClientId());
            predicates.add(criteriaBuilder.and(root.get("invoice").get("client").get("id").in(clientIds)));

            if (paymentSearchRequestDTO.getFromDate() != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), paymentSearchRequestDTO.getFromDate())));
            }
            if (paymentSearchRequestDTO.getToDate() != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), paymentSearchRequestDTO.getToDate())));
            }
//            if (!paymentSearchRequestDTO.getCustomerId().isEmpty() ) {
//                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("invoice").get("customer").get("identity"), paymentSearchRequestDTO.getCustomerId())));
//            }
            if (!paymentSearchRequestDTO.getPaymentStatus().equals(PaymentStatus.NONE)) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), paymentSearchRequestDTO.getPaymentStatus())));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }


    // payment report data
    List<PaymentDTO> getPaymentsBetween(ZonedDateTime start, ZonedDateTime end, Long clientId) {
        List<Long> clientIds = userService.listClientIds(clientId);

        return paymentMapper.toDto(paymentRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.and(root.get("invoice").get("client").get("id").in(clientIds)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), PaymentStatus.PAID)));
            if (start != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), start)));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), end)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }


    @Scheduled(cron = "${tbs.cron.payment-credit-card-correction}")
    public void paymentCorrectionJob() {
        ZonedDateTime from = ZonedDateTime.now().minusMinutes(30);
        ZonedDateTime to = ZonedDateTime.now().minusMinutes(5);
        log.debug("---- Run paymentCorrectionJob with params {} --- {}", from, to);
        List<Payment> payments = paymentRepository.findByStatusAndAndLastModifiedDateBetween(PaymentStatus.CHECKOUT_PAGE, from, to);

        log.debug("---- checking {} payments with status checkout page", payments.size());
        if (CollectionUtils.isNotEmpty(payments)) {
            for (Payment payment : payments) {
                PaymentStatusResponseDTO response = sTSPaymentService.checkOffilnePaymentStatus(payment.getTransactionId());
                if (Constants.STS_PAYMENT_SUCCESS_CODE.equalsIgnoreCase(response.getCode()) && payment.getStatus() == PaymentStatus.CHECKOUT_PAGE) {
                    // Notify Client app
                    Invoice invoice = payment.getInvoice();
                    if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "staging")
                        || CommonUtils.isProfile(environment, "ahmed")) {
                        sendPaymentNotificationToClient(clientMapper.toDto(invoice.getClient()), invoice.getAccountId(), payment);
                    } else {
                        log.warn("----Not prod/staging env, client notif desabled, Invoice: {}"+ invoice.getAccountId());
                    }
                }
            }
        }
    }

    public String getCustomerId(Customer customer) {
        if (customer.getIdentity() != null) {
            return customer.getIdentity();
        } else {
            return customer.getContact().getPhone();
        }
    }

}
