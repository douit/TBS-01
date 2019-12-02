package sa.tamkeentech.tbs.web.rest;

import org.springframework.boot.configurationprocessor.json.JSONException;
import sa.tamkeentech.tbs.service.RefundService;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.service.dto.RefundDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Refund}.
 */
@RestController
// @RequestMapping("")
public class RefundResource {

    private final Logger log = LoggerFactory.getLogger(RefundResource.class);

    private static final String ENTITY_NAME = "refund";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RefundService refundService;

    public RefundResource(RefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * {@code POST  /refunds} : Create a new refund.
     *
     * @param refundDTO the refundDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new refundDTO, or with status {@code 400 (Bad Request)} if the refund has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/billing/refunds")
    public ResponseEntity<RefundDTO> createRefund(@RequestBody RefundDTO refundDTO) throws URISyntaxException, IOException, JSONException {
        log.debug("REST request to save Refund : {}", refundDTO);
        if (refundDTO.getId() != null) {
            throw new BadRequestAlertException("A new refund cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RefundDTO result = refundService.createNewRefund(refundDTO);
        return ResponseEntity.created(new URI("/api/refunds/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }


    /**
     * {@code PUT  /refunds} : Updates an existing refund.
     *
     * @param refundDTO the refundDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated refundDTO,
     * or with status {@code 400 (Bad Request)} if the refundDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the refundDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/api/refunds")
    public ResponseEntity<RefundDTO> updateRefund(@RequestBody RefundDTO refundDTO) throws URISyntaxException {
        log.debug("REST request to update Refund : {}", refundDTO);
        if (refundDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RefundDTO result = refundService.save(refundDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, refundDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /refunds} : get all the refunds.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of refunds in body.
     */
    @GetMapping("/api/refunds")
    public List<RefundDTO> getAllRefunds() {
        log.debug("REST request to get all Refunds");
        return refundService.findAll();
    }

    /**
     * {@code GET  /refunds/:id} : get the "id" refund.
     *
     * @param id the id of the refundDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the refundDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/api/refunds/{id}")
    public ResponseEntity<RefundDTO> getRefund(@PathVariable Long id) {
        log.debug("REST request to get Refund : {}", id);
        Optional<RefundDTO> refundDTO = refundService.findOne(id);
        return ResponseUtil.wrapOrNotFound(refundDTO);
    }

    /**
     * {@code DELETE  /refunds/:id} : delete the "id" refund.
     *
     * @param id the id of the refundDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/api/refunds/{id}")
    public ResponseEntity<Void> deleteRefund(@PathVariable Long id) {
        log.debug("REST request to delete Refund : {}", id);
        refundService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
