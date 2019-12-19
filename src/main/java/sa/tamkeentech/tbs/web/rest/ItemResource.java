package sa.tamkeentech.tbs.web.rest;

import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sa.tamkeentech.tbs.domain.Tax;
import sa.tamkeentech.tbs.repository.TaxRepository;
import sa.tamkeentech.tbs.service.ItemService;
import sa.tamkeentech.tbs.service.dto.TaxDTO;
import sa.tamkeentech.tbs.service.mapper.TaxMapper;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;
import sa.tamkeentech.tbs.service.dto.ItemDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

/**
 * REST controller for managing {@link sa.tamkeentech.tbs.domain.Item}.
 */
@RestController
@RequestMapping("/api")
public class ItemResource {

    private final Logger log = LoggerFactory.getLogger(ItemResource.class);

    private static final String ENTITY_NAME = "item";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ItemService itemService;
    private final TaxRepository taxRepository;
    private final TaxMapper taxMapper;
    public ItemResource(ItemService itemService, TaxRepository taxRepository,  TaxMapper taxMapper) {
        this.itemService = itemService;
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
    }

    /**
     * {@code POST  /items} : Create a new item.
     *
     * @param itemDTO the itemDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new itemDTO, or with status {@code 400 (Bad Request)} if the item has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/items")
    public ResponseEntity<ItemDTO> createItem(@RequestBody ItemDTO itemDTO) throws URISyntaxException {
        log.debug("REST request to save Item : {}", itemDTO);
        if (itemDTO.getId() != null) {
            throw new BadRequestAlertException("A new item cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Set<TaxDTO> taxes = new HashSet<>();
        for(TaxDTO taxDTO : itemDTO.getTaxes()){
            if(taxRepository.findByName(taxDTO.getName()) != null){
                Optional<Tax> tax =taxRepository.findByName(taxDTO.getName());
                taxes.add(taxMapper.toDto(tax.get()));

            }else{
                throw new TbsRunTimeException("Tax doesn't exist");
            }
        }
        itemDTO.setTaxes(taxes);
        ItemDTO result = itemService.save(itemDTO);
        return ResponseEntity.created(new URI("/api/items/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /items} : Updates an existing item.
     *
     * @param itemDTO the itemDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated itemDTO,
     * or with status {@code 400 (Bad Request)} if the itemDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the itemDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/items")
    public ResponseEntity<ItemDTO> updateItem(@RequestBody ItemDTO itemDTO) throws URISyntaxException {
        log.debug("REST request to update Item : {}", itemDTO);
        if (itemDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ItemDTO result = itemService.save(itemDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, itemDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /items} : get all the items.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of items in body.
     */
    @GetMapping("/items")
    public List<ItemDTO> getAllItems() {
        log.debug("REST request to get all Items");
        return itemService.findAll();
    }

    /**
     * {@code GET  /items/:id} : get the "id" item.
     *
     * @param id the id of the itemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the itemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable Long id) {
        log.debug("REST request to get Item : {}", id);
        Optional<ItemDTO> itemDTO = itemService.findOne(id);
        return ResponseUtil.wrapOrNotFound(itemDTO);
    }

    /**
     * {@code DELETE  /items/:id} : delete the "id" item.
     *
     * @param id the id of the itemDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        log.debug("REST request to delete Item : {}", id);
        itemService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    // @PreAuthorize("isAuthenticated()")
    @GetMapping("/items/datatable")
    public DataTablesOutput<ItemDTO> getAllItems(DataTablesInput input) {
        return itemService.get(input);
    }
}
