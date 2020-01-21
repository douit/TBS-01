package sa.tamkeentech.tbs.web.rest;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.domain.PersistentAuditEvent;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.service.AuditEventService;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.service.dto.InvoiceSearchRequestDTO;
import sa.tamkeentech.tbs.service.dto.ItemDTO;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Invoice}.
 */
@RestController
@RequestMapping("/api")
public class InvoiceResource {

    private final Logger log = LoggerFactory.getLogger(InvoiceResource.class);

    private static final String ENTITY_NAME = "invoice";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InvoiceService invoiceService;

    private final AuditEventService auditEventService;

    public InvoiceResource(InvoiceService invoiceService, AuditEventService auditEventService) {
        this.invoiceService = invoiceService;
        this.auditEventService = auditEventService;
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(Pageable pageable) {
        log.debug("REST request to get a page of Invoices");
        Page<InvoiceDTO> page = invoiceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /invoices} : get all the invoices.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of invoices in body.
     */
    @GetMapping("/invoices/paymentStatus/{status}")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoicesByStatus(@PathVariable PaymentStatus status, Pageable pageable) {
        log.debug("REST request to get a page of Invoices");
        Page<InvoiceDTO> page = invoiceService.findByPaymentStatus(status, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /invoices/:id} : get the "id" invoice.
     *
     * @param id the id of the invoiceDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the invoiceDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceDTO> getInvoice(@PathVariable Long id) {
        log.debug("REST request to get Invoice : {}", id);
        Optional<InvoiceDTO> invoiceDTO = invoiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(invoiceDTO);
    }

    /**
     * {@code DELETE  /invoices/:id} : delete the "id" invoice.
     *
     * @param id the id of the invoiceDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    /*@DeleteMapping("/invoices/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        log.debug("REST request to delete Invoice : {}", id);
        invoiceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }*/

    /**
     *
     * @param input
     * @return
     */
    // @PreAuthorize("isAuthenticated()")
    @GetMapping("/invoices/datatable")
    public DataTablesOutput<InvoiceDTO> getAllItems(DataTablesInput input)
    {
        return invoiceService.get(input);
    }

    /**
     *
     * @param invoiceSearchRequestDTO
     * @return
     */
    @PostMapping("/invoices/search")
    @ResponseBody
    public DataTablesOutput<InvoiceDTO> getInvoiceBySearching( @RequestBody InvoiceSearchRequestDTO invoiceSearchRequestDTO) {
        log.debug("REST request to get a page of Invoices");
//        Page<InvoiceDTO> page = invoiceService.findAll(pageable);
//        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return invoiceService.getInvoiceByQuerySearch(invoiceSearchRequestDTO);
    }

    @GetMapping("/invoices/audit/{accountId}")
    public List<PersistentAuditEvent> getAudit(@PathVariable Long accountId) {
        return auditEventService.findInvoiceAudit(accountId);
    }
}
