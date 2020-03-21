package sa.tamkeentech.tbs.web.rest;

import sa.tamkeentech.tbs.service.InvoiceItemService;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.service.dto.InvoiceItemDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.InvoiceItem}.
 */
// @RestController
// @RequestMapping("/api")
public class InvoiceItemResource {

    private final Logger log = LoggerFactory.getLogger(InvoiceItemResource.class);

    private static final String ENTITY_NAME = "invoiceItem";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InvoiceItemService invoiceItemService;

    public InvoiceItemResource(InvoiceItemService invoiceItemService) {
        this.invoiceItemService = invoiceItemService;
    }

    /**
     * {@code POST  /invoice-items} : Create a new invoiceItem.
     *
     * @param invoiceItemDTO the invoiceItemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new invoiceItemDTO, or with status {@code 400 (Bad Request)} if the invoiceItem has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    // @PostMapping("/invoice-items")
    public ResponseEntity<InvoiceItemDTO> createInvoiceItem(@RequestBody InvoiceItemDTO invoiceItemDTO) throws URISyntaxException {
        log.debug("REST request to save InvoiceItem : {}", invoiceItemDTO);
        if (invoiceItemDTO.getId() != null) {
            throw new BadRequestAlertException("A new invoiceItem cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InvoiceItemDTO result = invoiceItemService.save(invoiceItemDTO);
        return ResponseEntity.created(new URI("/api/invoice-items/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /invoice-items} : Updates an existing invoiceItem.
     *
     * @param invoiceItemDTO the invoiceItemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated invoiceItemDTO,
     * or with status {@code 400 (Bad Request)} if the invoiceItemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the invoiceItemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    // @PutMapping("/invoice-items")
    public ResponseEntity<InvoiceItemDTO> updateInvoiceItem(@RequestBody InvoiceItemDTO invoiceItemDTO) throws URISyntaxException {
        log.debug("REST request to update InvoiceItem : {}", invoiceItemDTO);
        if (invoiceItemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        InvoiceItemDTO result = invoiceItemService.save(invoiceItemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, invoiceItemDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /invoice-items} : get all the invoiceItems.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of invoiceItems in body.
     */
    // @GetMapping("/invoice-items")
    public List<InvoiceItemDTO> getAllInvoiceItems() {
        log.debug("REST request to get all InvoiceItems");
        return invoiceItemService.findAll();
    }

    /**
     * {@code GET  /invoice-items/:id} : get the "id" invoiceItem.
     *
     * @param id the id of the invoiceItemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the invoiceItemDTO, or with status {@code 404 (Not Found)}.
     */
    // @GetMapping("/invoice-items/{id}")
    public ResponseEntity<InvoiceItemDTO> getInvoiceItem(@PathVariable Long id) {
        log.debug("REST request to get InvoiceItem : {}", id);
        Optional<InvoiceItemDTO> invoiceItemDTO = invoiceItemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(invoiceItemDTO);
    }

    /**
     * {@code DELETE  /invoice-items/:id} : delete the "id" invoiceItem.
     *
     * @param id the id of the invoiceItemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    // @DeleteMapping("/invoice-items/{id}")
    public ResponseEntity<Void> deleteInvoiceItem(@PathVariable Long id) {
        log.debug("REST request to delete InvoiceItem : {}", id);
        invoiceItemService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
