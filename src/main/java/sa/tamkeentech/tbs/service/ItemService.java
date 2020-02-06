package sa.tamkeentech.tbs.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
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

    private final UserService userService;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, TaxRepository taxRepository, TaxMapper taxMapper, CategoryRepository categoryRepository,  ClientService clientService, UserService userService) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
        this.categoryRepository = categoryRepository;
        this.clientService = clientService;
        this.userService = userService;
    }

    /**
     * Save a item.
     *
     * @param itemDTO the entity to save.
     * @return the persisted entity.
     */
    public ItemDTO save(ItemDTO itemDTO, boolean isApp) {
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
        if(itemDTO.isFlexiblePrice()){
            itemDTO.setPrice(BigDecimal.ZERO);
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
        Optional<Client> client = Optional.empty();
        if (isApp) {
            String appName = SecurityUtils.getCurrentUserLogin().orElse("");
            client =  clientService.getClientByClientId(appName);
        } else {
            client =  clientService.getClientById(itemDTO.getClient().getId());
        }

        if (!client.isPresent()) {
            throw new TbsRunTimeException("Client not Authorized");
        }
        if(StringUtils.isEmpty(itemDTO.getCode())){
            throw new TbsRunTimeException("Item code Type is mandatory");
        }
        if (itemDTO.getId() == null) {
            // check if code is unique per client
            if (itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId()).isPresent()) {
                throw new TbsRunTimeException("Item already created");
            }
        }else{
            Optional<Item> oldItem = itemRepository.findById(itemDTO.getId());
            if(oldItem.isPresent() && !oldItem.get().getCode().equalsIgnoreCase(itemDTO.getCode())){
                if (itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId()).isPresent()) {
                    throw new TbsRunTimeException("Item code exists");
                }
            }
        }

        item.setClient(client.get());

        item = itemRepository.save(item);
        ItemDTO itemResultDTO = itemMapper.toDto(item);
        return itemResultDTO;
    }


    public ItemDTO updateItemByApp(ItemDTO itemDTO) {

        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        if (!client.isPresent()) {
            throw new TbsRunTimeException("Client not Authorized");
        }
        Optional<Item> item = itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId());
        if (!item.isPresent()) {
            throw new TbsRunTimeException("Item not found");
        }
        // update tax
        Set<Tax> taxes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(itemDTO.getTaxes())) {
            for(TaxDTO taxDTO : itemDTO.getTaxes()){
                Optional<Tax> tax = taxRepository.findByCode(taxDTO.getCode());
                if (tax.isPresent()) {
                    taxes.add(tax.get());
                } else {
                    throw new TbsRunTimeException("Tax doesn't exist");
                }
            }
        }
        item.get().setTaxes(taxes);
        // update Category
        if(itemDTO.getCategory() != null && StringUtils.isNotEmpty(itemDTO.getCategory().getCode())) {
            // set category and client
            Optional<Category> category = categoryRepository.findByCode(itemDTO.getCategory().getCode());
            if (!category.isPresent()) {
                throw new TbsRunTimeException("Category doesn't exist");
            }
            item.get().setCategory(category.get());
        }
        // update price
        if (itemDTO.getPrice() != null) {
            item.get().setPrice(itemDTO.getPrice());
        }
        // update name
        if (StringUtils.isNotEmpty(itemDTO.getName())) {
            item.get().setName(itemDTO.getName());
        }
        // update default quantity
        if (itemDTO.getDefaultQuantity() != null) {
            item.get().setDefaultQuantity(itemDTO.getDefaultQuantity());
        }
        Item persitedItem = itemRepository.save(item.get());
        ItemDTO itemResultDTO = itemMapper.toDto(persitedItem);
        return itemResultDTO;
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

    public Optional<ItemDTO> findByCodeForCurrentClient(String code) {
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        if (!client.isPresent()) {
            return Optional.empty();
        }
        return itemRepository.findByCodeAndClientId(code, client.get().getId()).map(itemMapper::toDto);
    }

    @Transactional(readOnly = true)
    public DataTablesOutput<ItemDTO> get(DataTablesInput input) {
        // return itemMapper.toDto(itemRepository.findAll(input));

        return itemMapper.toDto(itemRepository.findAll(input, (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<Long> clientIds = userService.listClientIds(null);
            predicates.add(criteriaBuilder.and(root.get("client").get("id").in(clientIds)));
             return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }
}
