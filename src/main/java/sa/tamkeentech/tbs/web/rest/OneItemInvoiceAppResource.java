package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.PaymentMethodService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

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

    private final PaymentMethodService paymentMethodService;

    private final PaymentService paymentService;

    public OneItemInvoiceAppResource(InvoiceService invoiceService, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
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

}
