package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.PaymentMethodService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Invoice}.
 */
@RestController
//@RequestMapping("/billing")
public class OneItemInvoiceAppResource {

    private final Logger log = LoggerFactory.getLogger(OneItemInvoiceAppResource.class);

    private static final String ENTITY_NAME = "invoice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InvoiceService invoiceService;

    private final InvoiceRepository invoiceRepository;

    private final PaymentRepository paymentRepository;

    private final PaymentMethodService paymentMethodService;

    public OneItemInvoiceAppResource(InvoiceService invoiceService, InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, sa.tamkeentech.tbs.service.PaymentMethodService paymentMethodService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * {@code POST  /invoices} : Create a new invoice.
     *
     * @param oneItemInvoiceDTO the oneItemInvoiceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invoiceDTO, or with status {@code 400 (Bad Request)} if the invoice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/billing/createbill")
    public ResponseEntity<OneItemInvoiceRespDTO> createOneItemInvoice(@Valid @RequestBody OneItemInvoiceDTO oneItemInvoiceDTO) throws URISyntaxException {
        log.debug("REST request to save Invoice : {}", oneItemInvoiceDTO);
        if (oneItemInvoiceDTO.getBillNumber() != null) {
            throw new BadRequestAlertException("A new invoice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OneItemInvoiceRespDTO result = invoiceService.saveOneItemInvoice(oneItemInvoiceDTO);
        String id = (result.getBillNumber()!= null)? result.getBillNumber().toString(): "";
        return ResponseEntity.created(new URI("/billing/createbill" + result.getBillNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, id))
            .body(result);
    }

    @GetMapping("/billing/getBillbyBillNumber")
    public ResponseEntity<InvoiceStatusDTO> getInvoice(@RequestParam Long billNumber) throws URISyntaxException {
        InvoiceStatusDTO result = invoiceService.getOneItemInvoice(billNumber);
        return ResponseEntity.created(new URI("/billing/getBillbyBillNumber" + result.getBillNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, billNumber.toString()))
            .body(result);
    }


    @PostMapping(value="/sadad/paymentnotification")
    ResponseEntity<NotifiRespDTO>  getPaymentNotification(@RequestBody NotifiReqDTO req, @RequestHeader(value="TBS-ApiKey")  String apiKey , @RequestHeader(value="TBS-ApiSecret")  String apiSecret)  throws Exception {

        // Optional<Invoice> invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount())-7000000065l);
        Optional<Invoice> invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount()));
        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(Constants.SADAD);
        Payment payment = Payment.builder()
            .invoice(invoice.get())
            .status(PaymentStatus.SUCCESSFUL)
            .amount(new BigDecimal(req.getAmount()))
            .paymentMethod(paymentMethod.get())
            //.expirationDate()
            .build();
        paymentRepository.save(payment);

        NotifiRespDTO resp = NotifiRespDTO.builder().statusId(200).build();
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
            HttpEntity<MultiValueMap<String, String>> request1 = new HttpEntity<MultiValueMap<String, String>>(map1, headers1);
            //uri = "https://sso.tamkeen.land/auth/realms/tamkeen/protocol/openid-connect/token"; // staging
            String uri = "https://accounts.wahid.sa/auth/realms/wahid/protocol/openid-connect/token"; // production
            ResponseEntity<TokenResponseDTO> response1 = rt1.postForEntity( uri, request1 , TokenResponseDTO.class );
            log.info("DVS Token" +  response1.getBody().getAccess_token());
            RestTemplate restTemplate = new RestTemplate();
            String ResourceUrl = "http://10.60.71.16:8880/dvs/?billnumber=";
            ResponseEntity<NotifiRespDTO> response2= restTemplate.getForEntity(ResourceUrl + req.getBillAccount() + "&paymentdate=" + req.getPaymentDate() + "&token=" + response1.getBody().getAccess_token() , NotifiRespDTO.class);
            log.info("Succuss DVS update" + response2.getBody().getStatusId());
            // NotifiResp resp = (NotifiResp)response2.getBody(); // only for testing
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
        } else {
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
