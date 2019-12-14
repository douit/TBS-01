package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;
import sa.tamkeentech.tbs.service.dto.InvoiceItemsResponseDTO;
import sa.tamkeentech.tbs.service.dto.OneItemInvoiceDTO;
import sa.tamkeentech.tbs.service.dto.OneItemInvoiceRespDTO;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Invoice}.
 */
@RestController
@RequestMapping("/billing")
public class InvoiceAppResource {

    private final Logger log = LoggerFactory.getLogger(InvoiceAppResource.class);

    private static final String ENTITY_NAME = "invoice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InvoiceService invoiceService;

    public InvoiceAppResource(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * {@code POST  /invoices} : Create a new invoice.
     *
     * @param oneItemInvoiceDTO the oneItemInvoiceDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invoiceDTO, or with status {@code 400 (Bad Request)} if the invoice has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/invoices")
    public ResponseEntity<OneItemInvoiceRespDTO> createOneItemInvoice(@Valid @RequestBody OneItemInvoiceDTO oneItemInvoiceDTO) throws URISyntaxException {
        log.debug("REST request to save Invoice : {}", oneItemInvoiceDTO);
        if (oneItemInvoiceDTO.getBillNumber() != null) {
            throw new BadRequestAlertException("A new invoice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OneItemInvoiceRespDTO result = invoiceService.saveOneItemInvoiceAndSendEvent(oneItemInvoiceDTO);
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

    @PostMapping("/invoice")
    public ResponseEntity<InvoiceItemsResponseDTO> creatInvoice(@Valid @RequestBody InvoiceDTO invoiceDTO) throws URISyntaxException {
        log.debug("REST request to save Invoice Items : {}", invoiceDTO);
        if (invoiceDTO.getBillNumber() != null) {
            throw new BadRequestAlertException("A new invoice cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InvoiceItemsResponseDTO result = invoiceService.saveInvoice(invoiceDTO);
        String id = (result.getBillNumber()!= null)? result.getBillNumber().toString(): "";
        return ResponseEntity.created(new URI("/api/invoices/" + result.getBillNumber()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, id))
            .body(result);
    }

}
