package sa.tamkeentech.tbs.web.rest;

import sa.tamkeentech.tbs.service.TaxService;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.service.dto.TaxDTO;

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
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Tax}.
 */
@RestController
@RequestMapping("/api")
public class TaxResource {

    private final Logger log = LoggerFactory.getLogger(TaxResource.class);

    private static final String ENTITY_NAME = "tax";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaxService taxService;

    public TaxResource(TaxService taxService) {
        this.taxService = taxService;
    }

    /**
     * {@code POST  /taxes} : Create a new tax.
     *
     * @param taxDTO the taxDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taxDTO, or with status {@code 400 (Bad Request)} if the tax has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/taxes")
    public ResponseEntity<TaxDTO> createTax(@RequestBody TaxDTO taxDTO) throws URISyntaxException {
        log.debug("REST request to save Tax : {}", taxDTO);
        if (taxDTO.getId() != null) {
            throw new BadRequestAlertException("A new tax cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TaxDTO result = taxService.save(taxDTO);
        return ResponseEntity.created(new URI("/api/taxes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /taxes} : Updates an existing tax.
     *
     * @param taxDTO the taxDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taxDTO,
     * or with status {@code 400 (Bad Request)} if the taxDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taxDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/taxes")
    public ResponseEntity<TaxDTO> updateTax(@RequestBody TaxDTO taxDTO) throws URISyntaxException {
        log.debug("REST request to update Tax : {}", taxDTO);
        if (taxDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TaxDTO result = taxService.save(taxDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taxDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /taxes} : get all the taxes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of taxes in body.
     */
    @GetMapping("/taxes")
    public List<TaxDTO> getAllTaxes() {
        log.debug("REST request to get all Taxes");
        return taxService.findAll();
    }

    /**
     * {@code GET  /taxes/:id} : get the "id" tax.
     *
     * @param id the id of the taxDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taxDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/taxes/{id}")
    public ResponseEntity<TaxDTO> getTax(@PathVariable Long id) {
        log.debug("REST request to get Tax : {}", id);
        Optional<TaxDTO> taxDTO = taxService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taxDTO);
    }

    /**
     * {@code DELETE  /taxes/:id} : delete the "id" tax.
     *
     * @param id the id of the taxDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    /*@DeleteMapping("/taxes/{id}")
    public ResponseEntity<Void> deleteTax(@PathVariable Long id) {
        log.debug("REST request to delete Tax : {}", id);
        taxService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }*/
}
