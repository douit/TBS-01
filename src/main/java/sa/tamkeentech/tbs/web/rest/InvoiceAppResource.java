package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Invoice}.
 */
@RestController
// @RequestMapping("/billing")
public class InvoiceAppResource {

    private final Logger log = LoggerFactory.getLogger(InvoiceAppResource.class);

    private static final String ENTITY_NAME = "invoice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InvoiceService invoiceService;

    private final PaymentService paymentService;



    public InvoiceAppResource(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    /**
     * {@code POST  /invoices} : Create a new invoice.
     *
     * @param oneItemInvoiceDTO the oneItemInvoiceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invoiceDTO, or with status {@code 400 (Bad Request)} if the invoice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/billing/createbill")
    public ResponseEntity<InvoiceResponseDTO> createOneItemInvoice(@Valid @RequestBody OneItemInvoiceDTO oneItemInvoiceDTO) throws URISyntaxException {
        log.debug("REST request to save Invoice : {}", oneItemInvoiceDTO);
        if (oneItemInvoiceDTO.getBillNumber() != null) {
            throw new BadRequestAlertException("A new invoice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InvoiceResponseDTO result = invoiceService.saveOneItemInvoiceAndSendEvent(oneItemInvoiceDTO);
        String id = (result.getBillNumber()!= null)? result.getBillNumber().toString(): "";
        return ResponseEntity.created(new URI("/api/invoices/" + result.getBillNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, id))
            .body(result);
    }

    /**
     * {@code POST  /invoiceItems} : Create a new invoice.
     *
     * @param invoiceDTO the InvoiceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invoiceDTO, or with status {@code 400 (Bad Request)} if the invoice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */

    @PostMapping("/billing/invoice")
    public ResponseEntity<InvoiceResponseDTO> creatInvoice(@Valid @RequestBody InvoiceDTO invoiceDTO,
             @RequestHeader(value = "accept-language", defaultValue = Constants.DEFAULT_HEADER_LANGUAGE) String language) throws URISyntaxException {
        log.debug("REST request to save Invoice Items : {}", invoiceDTO);
        if (invoiceDTO.getBillNumber() != null) {
            throw new BadRequestAlertException("A new invoice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InvoiceResponseDTO result = invoiceService.saveInvoiceAndSendEvent(invoiceDTO);
        invoiceService.addExtraPaymentInfo(result, language);
        String id = (result.getBillNumber()!= null)? result.getBillNumber().toString(): "";
        return ResponseEntity.created(new URI("/api/invoices/" + result.getBillNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, id))
            .body(result);
    }

    @GetMapping("/billing/getBillbyBillNumber")
    public ResponseEntity<InvoiceStatusDTO> getInvoiceStatus(@RequestParam Long billNumber) throws URISyntaxException {
        InvoiceStatusDTO result = invoiceService.getOneItemInvoice(billNumber);
        return ResponseEntity.ok().body(result);
    }


    @PostMapping(value="/sadad/paymentnotification")
    ResponseEntity<NotifiRespDTO>  getPaymentNotification(@RequestBody NotifiReqDTO req, /*@RequestHeader(value="TBS-ApiKey")*/  String apiKey , /*@RequestHeader(value="TBS-ApiSecret")*/  String apiSecret)  throws Exception {
        return paymentService.sendEventAndPaymentNotification(req, apiKey, apiSecret);
    }

    // possible values SADAD or CREDIT_CARD
    @GetMapping("/billing/newPayment/{referenceId}/{paymentMethodCode}")
    public ResponseEntity<InvoiceResponseDTO> getPayment(@PathVariable String referenceId, @PathVariable String paymentMethodCode) {
        log.debug("REST request to change payment method Payment to : {}", paymentMethodCode);
        InvoiceResponseDTO resp = paymentService.requestNewPayment(referenceId, paymentMethodCode);
        return new ResponseEntity<InvoiceResponseDTO>(resp,  HttpStatus.OK);
    }

    @GetMapping("/billing/invoice/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id,
                                                 @RequestHeader(value = "accept-language", defaultValue = Constants.DEFAULT_HEADER_LANGUAGE) String language) {
        log.debug("REST request to get Invoice : {}", id);
        // Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<InvoiceDTO> invoiceDTO = invoiceService.findByAccountId(id);
        // stopwatch.stop(); // optional
        // log.info("--InvoiceGet 1--Time elapsed: "+ stopwatch.elapsed(TimeUnit.MILLISECONDS));

        // Stopwatch stopwatch2 = Stopwatch.createStarted();
        if (invoiceDTO.isPresent()) {
            invoiceDTO.get().setClient(null);
            invoiceService.addExtraPaymentInfo(invoiceDTO.get(), language);
            // moved to service
            /*invoiceDTO.get().setVatNumber("300879111900003");
            Optional<PersistentAuditEvent> event = persistenceAuditEventRepository.findFirstByRefIdAndSuccessfulAndAuditEventTypeOrderByIdDesc(invoiceDTO.get().getAccountId(), true, Constants.EventType.SADAD_INITIATE.name());
            if (event.isPresent()) {
                invoiceDTO.get().setBillerId(156);
            }
            String lang = StringUtils.isNotEmpty(language)? language: Constants.LANGUAGE.ARABIC.getHeaderKey();
            invoiceDTO.get().setCompanyName(languageUtil.getMessageByKey("company.name", Constants.LANGUAGE.getLanguageByHeaderKey(lang)));*/

        }
        // stopwatch2.stop(); // optional
        // log.info("--InvoiceGet 2--Time elapsed: "+ stopwatch2.elapsed(TimeUnit.MILLISECONDS));
        return ResponseUtil.wrapOrNotFound(invoiceDTO);
    }

    @GetMapping("/billing/invoice/customer/{customerId}")
    public List<InvoiceDTO> getInvoicesByCustomer(@PathVariable String customerId,
                                                 @RequestHeader(value = "accept-language", defaultValue = Constants.DEFAULT_HEADER_LANGUAGE) String language) {
        log.debug("REST request to get Invoices for customer : {}", customerId);
        if (StringUtils.isEmpty(customerId)) {
            throw new TbsRunTimeException("customerId is mandatory");
        }
        return invoiceService.findByCustomerId(customerId, language);
    }

}
