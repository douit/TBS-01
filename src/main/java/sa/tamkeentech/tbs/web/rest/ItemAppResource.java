package sa.tamkeentech.tbs.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.domain.Item;
import sa.tamkeentech.tbs.repository.ItemRepository;
import sa.tamkeentech.tbs.repository.TaxRepository;
import sa.tamkeentech.tbs.service.ItemService;
import sa.tamkeentech.tbs.service.dto.ItemDTO;
import sa.tamkeentech.tbs.service.mapper.TaxMapper;
import sa.tamkeentech.tbs.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing {@link Item}.
 */
@RestController
// @RequestMapping("/api")
@RequestMapping("/billing")
public class ItemAppResource {

    private final Logger log = LoggerFactory.getLogger(ItemAppResource.class);

    private static final String ENTITY_NAME = "item";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    public ItemAppResource(ItemService itemService, TaxRepository taxRepository, TaxMapper taxMapper, ItemRepository itemRepository) {
        this.itemService = itemService;
        this.itemRepository = itemRepository;
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

        ItemDTO result = itemService.save(itemDTO, true);
        if (result != null) {
            result.setClient(null);
        }
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
        log.debug("REST request from app to update Item : {}", itemDTO);
        if (itemDTO.getCode() == null) {
            throw new BadRequestAlertException("Invalid Code", ENTITY_NAME, " code null");
        }
        // no code update --> get by code then update
        ItemDTO result = itemService.updateItemByApp(itemDTO);//.save(itemDTO, true);
        if (result != null) {
            result.setClient(null);
        }

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
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
        List<ItemDTO> items = itemService.findAll();
        if (CollectionUtils.isNotEmpty(items)) {
            return items.stream().map(item -> {
                item.setClient(null);
                return item;
            }).collect(Collectors.toList());
        }
        return items;
    }

    /**
     * {@code GET  /items/:id} : get the "id" item.
     *
     * @param code the id of the itemDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the itemDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/item/{code}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable String code) {
        log.debug("REST request to get Item from app : {}", code);
        Optional<ItemDTO> itemDTO = itemService.findByCodeForCurrentClient(code);
        if (itemDTO.isPresent()) {
            itemDTO.get().setClient(null);
        }
        return ResponseUtil.wrapOrNotFound(itemDTO);
    }

}
