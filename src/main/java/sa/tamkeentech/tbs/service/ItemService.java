package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Category;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.Item;
import sa.tamkeentech.tbs.domain.Tax;
import sa.tamkeentech.tbs.repository.CategoryRepository;
import sa.tamkeentech.tbs.repository.ItemRepository;
import sa.tamkeentech.tbs.repository.TaxRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.ItemDTO;
import sa.tamkeentech.tbs.service.dto.TaxDTO;
import sa.tamkeentech.tbs.service.mapper.ItemMapper;
import sa.tamkeentech.tbs.service.mapper.TaxMapper;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Item}.
 */
@Service
@Transactional
public class ItemService {

    private final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    private final TaxRepository taxRepository;

    private final TaxMapper taxMapper;

    private final CategoryRepository categoryRepository;

    private final ClientService clientService;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, TaxRepository taxRepository, TaxMapper taxMapper, CategoryRepository categoryRepository, ClientService clientService) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
        this.categoryRepository = categoryRepository;
        this.clientService = clientService;
    }

    /**
     * Save a item.
     *
     * @param itemDTO the entity to save.
     * @return the persisted entity.
     */
    public ItemDTO save(ItemDTO itemDTO) {
        log.debug("Request to save Item : {}", itemDTO);
        Set<Tax> taxes = new HashSet<>();
        for(TaxDTO taxDTO : itemDTO.getTaxes()){
            Optional<Tax> tax = taxRepository.findByCode(taxDTO.getCode());
            if (tax.isPresent()) {
                taxes.add(tax.get());

            } else {
                throw new TbsRunTimeException("Tax doesn't exist");
            }
        }
        Item item = itemMapper.toEntity(itemDTO);
        item.setTaxes(taxes);

        // set category and client
        Optional<Category> category = categoryRepository.findByCode(itemDTO.getCategory().getCode());
        if (!category.isPresent()) {
            throw new TbsRunTimeException("Category doesn't exist");
        }
        item.setCategory(category.get());
        // Client
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        if (!client.isPresent()) {
            throw new TbsRunTimeException("Client not Authorized");
        }
        item.setClient(client.get());

        item = itemRepository.save(item);
        return itemMapper.toDto(item);
    }

    /**
     * Get all the items.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<ItemDTO> findAll() {
        log.debug("Request to get all Items");
        return itemRepository.findAll().stream()
            .map(itemMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one item by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ItemDTO> findOne(Long id) {
        log.debug("Request to get Item : {}", id);
        return itemRepository.findById(id)
            .map(itemMapper::toDto);
    }

    /**
     * Delete the item by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Item : {}", id);
        itemRepository.deleteById(id);
    }

    public Optional<Item> findByCodeAndClient(String code, Long id) {
        return itemRepository.findByCodeAndClientId(code, id);
    }

    public DataTablesOutput<ItemDTO> get(DataTablesInput input) {
        return itemMapper.toDto(itemRepository.findAll(input));
    }
}
