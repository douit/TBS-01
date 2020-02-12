package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
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
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.InvoiceStatus;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.repository.PersistenceAuditEventRepository;
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
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    @Value("${tbs.payment.credit-card-url}")
    private String creditCardUrl;

    @Value("${tbs.payment.credit-card-biller-code}")
    private String billerCode;

    @Autowired
    private Environment environment;

    @Autowired
    private UserService userService;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, ObjectMapper objectMapper, EventPublisherService eventPublisherService, ClientService clientService, ClientMapper clientMapper, PersistenceAuditEventRepository persistenceAuditEventRepository, PaymentMethodMapper paymentMethodMapper, ClientRepository clientRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
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

    /**
     *
     * @param req
     * @param invoice
     * @return
     */
    @Transactional
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
    }

    /**
     *  Update Credit card payment status
     *
     * @param paymentStatusResponseDTO
     * @return
     */
    @Transactional
    public PaymentDTO updateCreditCardPaymentAndSendEvent(PaymentStatusResponseDTO paymentStatusResponseDTO) {
        log.debug("Request to update status Payment : {}", paymentStatusResponseDTO);
        Payment payment = paymentRepository.findByTransactionId(paymentStatusResponseDTO.getTransactionId());
        Invoice invoice = payment.getInvoice();

        TBSEventReqDTO<PaymentStatusResponseDTO> reqNotification = TBSEventReqDTO.<PaymentStatusResponseDTO>builder().principalId(invoice.getCustomer().getIdentity())
            .referenceId(invoice.getAccountId().toString()).req(paymentStatusResponseDTO).build();

        return eventPublisherService.creditCardNotificationEvent(reqNotification, payment, invoice).getResp();
    }

    @Transactional
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

    @Transactional
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

    @Transactional
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

    // @Transactional
    public ResponseEntity<NotifiRespDTO> sendEventAndPaymentNotification(NotifiReqDTO req, String apiKey, String apiSecret) {
        log.debug("----Sadad Notification : {}", req);
        Invoice invoice = invoiceRepository.findByAccountId(Long.parseLong(req.getBillAccount())).orElse(null);

        TBSEventReqDTO<NotifiReqDTO> reqNotification = TBSEventReqDTO.<NotifiReqDTO>builder()
            .principalId((invoice != null)? invoice.getCustomer().getIdentity(): null)
            .referenceId((invoice != null)?invoice.getAccountId().toString():null)
            .req(req).build();
        NotifiRespDTO resp = eventPublisherService.sendPaymentNotification(reqNotification, invoice).getResp();

        return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
    }

    // @Transactional
    public NotifiRespDTO sendPaymentNotification(NotifiReqDTO reqNotification, Invoice invoice) {

        if (invoice != null) {
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
                    if (CommonUtils.isProfile(environment, "prod")) {
                        sendPaymentNotificationToClient(clientMapper.toDto(invoice.getClient()), invoice.getAccountId(), payment);
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

    @Scheduled(cron = "0  5  *  *  * ?")
    public void sendPaymentNotificationToClintJob(){

        List<ClientDTO> clients =  clientService.findAll();
        for(ClientDTO clientDTO : clients){
            List<Optional<Invoice>> invoices = invoiceRepository.findByStatusAndClient(InvoiceStatus.WAITING,clientMapper.toEntity(clientDTO));
            for (Optional<Invoice> invoice : invoices) {
                Optional<Payment> payment = paymentRepository.findFirstByInvoiceAccountIdAndStatus(invoice.get().getAccountId(), PaymentStatus.PAID);
                if (payment.isPresent()) {
                    // call sso only in prod, Later limit this to Tahaquq to avoid timeout and Sentry issue
                    if (CommonUtils.isProfile(environment, "prod")) {
                        sendPaymentNotificationToClient(clientDTO, invoice.get().getAccountId(), payment.get());
                    } else {
                        log.warn("----Not prod env, Invoice status is waiting and payment found, Invoice: {}"+ invoice.get().getAccountId());
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
        Date tokenModifiedDate = new Date();
        if (client.getTokenModifiedDate() != null) {
            tokenModifiedDate = Date.from(client.getTokenModifiedDate().toInstant());

        }
        long diff = currentDate.getTime() - tokenModifiedDate.getTime();
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);


        String token = null;
        // if (diffDays >= 1 || diffHours >= 1 || diffMinutes > 4 || client.getClientToken() == null ) {
            RestTemplate rt1 = new RestTemplate();
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
            ResponseEntity<TokenResponseDTO> response1 = rt1.postForEntity(uri, request1, TokenResponseDTO.class);
            token = response1.getBody().getAccess_token();

            Optional<Client> clientEntity = clientRepository.findById(client.getId());
            clientEntity.get().setClientToken(token);
            clientEntity.get().setTokenModifiedDate(currentDate.toInstant().atZone(ZoneId.systemDefault()));
            clientRepository.save(clientEntity.get());

            // log.info("DVS Token" + token);
        /*} else {
            token = client.getClientToken();
        }*/


        RestTemplate restTemplate = new RestTemplate();
        String pattern = " dd/MM/yyyy hh:mm:ss a";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String paymentDate = simpleDateFormat.format(Date.from(payment.getCreatedDate().toInstant()));
        String resourceUrl = "";
        /*if(Arrays.stream(environment.getActiveProfiles()).anyMatch(
            env -> (env.equalsIgnoreCase("prod")) )) {*/
        if (CommonUtils.isProfile(environment, "prod")) {
        // if (CommonUtils.isProdOrStaging(environment)) {
            resourceUrl += client.getNotificationUrl();
        } else {
            resourceUrl += "https://test";
        }
        // resourceUrl += ("?billnumber=" + accountId.toString() + "&paymentdate=" +  paymentDate + "&token=" + token);
        log.info("----calling Client update: "+ resourceUrl);
        // ResponseEntity<NotifiRespDTO> response2= restTemplate.getForEntity(resourceUrl, NotifiRespDTO.class);

        //Mule Post query
        PaymentNotifReqToClientDTO paymentNotifReqToClientDTO = PaymentNotifReqToClientDTO.builder()
            .billNumber(accountId.toString())
            .paymentDate(paymentDate)
            .status("paid")
            .paymentMethod(new PaymentNotifReqToClientDTO.PaymentInternalInfo("1", "SADAD"))
            .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+token);
        headers.set("Content-Type", "application/json");
        headers.set("client_id", environment.getProperty("tbs.payment.notification-client-id"));
        headers.set("client_secret", environment.getProperty("tbs.payment.notification-client-secret"));

        HttpEntity<PaymentNotifReqToClientDTO> request = new HttpEntity<>(paymentNotifReqToClientDTO, headers);
        ResponseEntity<PaymentNotifResFromClientDTO> response2= null;
        try {
            response2 = restTemplate.exchange(resourceUrl, HttpMethod.POST, request, PaymentNotifResFromClientDTO.class);
        } catch (HttpServerErrorException e) {
            log.error("Payment notif Response satatus:{}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            e.printStackTrace();
            Sentry.capture(e);
            // in case parsing is needed
            /*if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                String responseString = e.getResponseBodyAsString();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    PaymentNotifResFromClientDTO res = mapper.readValue(responseString, PaymentNotifResFromClientDTO.class);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }*/
        }

        if (response2 != null && response2.getBody()!= null && response2.getBody().getStatusId() == 1) {
            log.info("----Successful Client update: " + response2.getBody().getStatusId());
            Optional<Invoice> invoice = invoiceRepository.findByAccountId(accountId);
            invoice.get().setStatus(InvoiceStatus.CLIENT_NOTIFIED);
            invoiceRepository.save(invoice.get());
        } else {
            log.info("----Issue Client update: {} -- status code: {}", ((response2 != null && response2.getBody()!= null)? response2.getBody().getStatusId(): "Empty body"), (response2 != null)? response2.getStatusCode(): "Null resp");
        }
    }

    @Transactional
    public InvoiceResponseDTO changePaymentMethod(String referenceId, String paymentMethodCode) {

        if (!Constants.SADAD.equals(paymentMethodCode) && !Constants.CREDIT_CARD.equals(paymentMethodCode)) {
            throw new TbsRunTimeException("Unkown payment method");
        }
        Optional<Invoice> invoice = invoiceRepository.findByAccountId(Long.parseLong(referenceId));
        if (!invoice.isPresent()) {
            throw new TbsRunTimeException("Invoice does not exist");
        }
        if (invoice.get().getPaymentStatus() == PaymentStatus.PAID) {
            throw new TbsRunTimeException("Invoice already paid");
        }
        InvoiceResponseDTO invoiceResponseDTO = InvoiceResponseDTO.builder().statusId(1).shortDesc("success").description("").build();
        invoiceResponseDTO.setBillNumber(invoice.get().getAccountId().toString());
        if (paymentMethodCode.equals(Constants.SADAD)) {
            // check if Sadad already called in event
            Optional<PersistentAuditEvent> event = persistenceAuditEventRepository.findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(Long.parseLong(referenceId), true, Constants.EventType.SADAD_INITIATE.name());
            if (!event.isPresent()) {
                int sadadResult;

                try {
                    sadadResult = sendEventAndCallSadad(invoice.get().getNumber(), invoice.get().getAccountId().toString(), invoice.get().getAmount(), invoice.get().getCustomer().getIdentity());
                } catch (IOException | JSONException e) {
                    throw new PaymentGatewayException("Sadad issue");
                }
                if (sadadResult != 200) {
                    throw new PaymentGatewayException("Sadad bill creation error");
                }
            }

        } else {
            Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(paymentMethodCode);
            PaymentMethodDTO paymentMethodDTO = paymentMethodMapper.toDto(paymentMethod.get());
            PaymentDTO paymentDTO = PaymentDTO.builder().invoiceId(invoice.get().getAccountId()).paymentMethod(paymentMethodDTO).build();
            paymentDTO = prepareCreditCardPayment(paymentDTO);
            invoiceResponseDTO.setLink(paymentDTO.getRedirectUrl());
        }
        return invoiceResponseDTO;
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
}
