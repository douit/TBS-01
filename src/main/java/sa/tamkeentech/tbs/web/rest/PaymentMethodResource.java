package sa.tamkeentech.tbs.web.rest;

import sa.tamkeentech.tbs.service.PaymentMethodService;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.service.dto.PaymentMethodDTO;

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
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.PaymentMethod}.
 */
@RestController
@RequestMapping("/api")
public class PaymentMethodResource {

    private final Logger log = LoggerFactory.getLogger(PaymentMethodResource.class);

    private static final String ENTITY_NAME = "paymentMethod";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodResource(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * {@code POST  /payment-methods} : Create a new paymentMethod.
     *
     * @param paymentMethodDTO the paymentMethodDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new paymentMethodDTO, or with status {@code 400 (Bad Request)} if the paymentMethod has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/payment-methods")
    public ResponseEntity<PaymentMethodDTO> createPaymentMethod(@RequestBody PaymentMethodDTO paymentMethodDTO) throws URISyntaxException {
        log.debug("REST request to save PaymentMethod : {}", paymentMethodDTO);
        if (paymentMethodDTO.getId() != null) {
            throw new BadRequestAlertException("A new paymentMethod cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PaymentMethodDTO result = paymentMethodService.save(paymentMethodDTO);
        return ResponseEntity.created(new URI("/api/payment-methods/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /payment-methods} : Updates an existing paymentMethod.
     *
     * @param paymentMethodDTO the paymentMethodDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated paymentMethodDTO,
     * or with status {@code 400 (Bad Request)} if the paymentMethodDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the paymentMethodDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/payment-methods")
    public ResponseEntity<PaymentMethodDTO> updatePaymentMethod(@RequestBody PaymentMethodDTO paymentMethodDTO) throws URISyntaxException {
        log.debug("REST request to update PaymentMethod : {}", paymentMethodDTO);
        if (paymentMethodDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PaymentMethodDTO result = paymentMethodService.save(paymentMethodDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, paymentMethodDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /payment-methods} : get all the paymentMethods.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of paymentMethods in body.
     */
    @GetMapping("/payment-methods")
    public List<PaymentMethodDTO> getAllPaymentMethods() {
        log.debug("REST request to get all PaymentMethods");
        return paymentMethodService.findAll();
    }

    /**
     * {@code GET  /payment-methods/:id} : get the "id" paymentMethod.
     *
     * @param id the id of the paymentMethodDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the paymentMethodDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/payment-methods/{id}")
    public ResponseEntity<PaymentMethodDTO> getPaymentMethod(@PathVariable Long id) {
        log.debug("REST request to get PaymentMethod : {}", id);
        Optional<PaymentMethodDTO> paymentMethodDTO = paymentMethodService.findOne(id);
        return ResponseUtil.wrapOrNotFound(paymentMethodDTO);
    }

    /**
     * {@code DELETE  /payment-methods/:id} : delete the "id" paymentMethod.
     *
     * @param id the id of the paymentMethodDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    // @DeleteMapping("/payment-methods/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        log.debug("REST request to delete PaymentMethod : {}", id);
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
