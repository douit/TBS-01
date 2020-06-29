package sa.tamkeentech.tbs.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.repository.CategoryRepository;
import sa.tamkeentech.tbs.repository.ItemHistoryRepository;
import sa.tamkeentech.tbs.repository.ItemRepository;
import sa.tamkeentech.tbs.repository.TaxRepository;
import sa.tamkeentech.tbs.security.SecurityUtils;
import sa.tamkeentech.tbs.service.dto.ItemDTO;
import sa.tamkeentech.tbs.service.dto.ItemHistoryDTO;
import sa.tamkeentech.tbs.service.dto.TaxDTO;
import sa.tamkeentech.tbs.service.mapper.ItemHistoryMapper;
import sa.tamkeentech.tbs.service.mapper.ItemMapper;
import sa.tamkeentech.tbs.service.mapper.TaxMapper;
import sa.tamkeentech.tbs.service.util.LanguageUtil;
import sa.tamkeentech.tbs.web.rest.errors.ItemAlreadyUsedException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
    private final LanguageUtil languageUtil;

    private final ItemHistoryMapper itemHistoryMapper;
    private final ItemHistoryRepository itemHistoryRepository;

    public ItemService(ItemRepository itemRepository, ItemMapper itemMapper, TaxRepository taxRepository, TaxMapper taxMapper, CategoryRepository categoryRepository, ClientService clientService, UserService userService, LanguageUtil languageUtil, ItemHistoryMapper itemHistoryMapper, ItemHistoryRepository itemHistoryRepository) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
        this.categoryRepository = categoryRepository;
        this.clientService = clientService;
        this.userService = userService;
        this.languageUtil = languageUtil;
        this.itemHistoryMapper = itemHistoryMapper;
        this.itemHistoryRepository = itemHistoryRepository;
    }

    /**
     * Save a item.
     *
     * @param itemDTO the entity to save.
     * @return the persisted entity.
     */
    public ItemDTO save(ItemDTO itemDTO, boolean isApp, String language) {
        log.debug("Request to save Item : {}", itemDTO);
        Set<Tax> taxes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(itemDTO.getTaxes())) {
            for(TaxDTO taxDTO : itemDTO.getTaxes()){
                Optional<Tax> tax = taxRepository.findByCode(taxDTO.getCode());
                if (tax.isPresent()) {
                    taxes.add(tax.get());

                } else {
                    throw new TbsRunTimeException(languageUtil.getMessageByKey("tax.does.not.exist", Constants.LANGUAGE.getLanguageByHeaderKey(language)));

                }
            }
        }

        if(itemDTO.isFlexiblePrice()){
            itemDTO.setPrice(BigDecimal.ZERO);
        } else if (itemDTO.getPrice() == null || itemDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TbsRunTimeException(languageUtil.getMessageByKey("wrong.price", Constants.LANGUAGE.getLanguageByHeaderKey(language)));

        }
        if (StringUtils.isNotEmpty(itemDTO.getCode())) {
            itemDTO.setCode(itemDTO.getCode().trim());
        }
        Item item = itemMapper.toEntity(itemDTO);
        item.setTaxes(taxes);

        // set category and client
        Optional<Category> category = categoryRepository.findByCode(itemDTO.getCategory().getCode());
        if (!category.isPresent()) {
            throw new TbsRunTimeException(languageUtil.getMessageByKey("category.does.not.exist", Constants.LANGUAGE.getLanguageByHeaderKey(language)));

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
            throw new TbsRunTimeException(languageUtil.getMessageByKey("client.not.authorized", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
        }
        if(StringUtils.isEmpty(itemDTO.getCode())){
            throw new TbsRunTimeException(languageUtil.getMessageByKey("item.code.type.mandatory", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
        }

        if (itemDTO.getId() == null) {
            // check if code is unique per client
            if (itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId()).isPresent()) {
                throw new TbsRunTimeException(languageUtil.getMessageByKey("item.already.created", Constants.LANGUAGE.getLanguageByHeaderKey(language)));

            }
        }else{
            Optional<Item> oldItem = itemRepository.findById(itemDTO.getId());
            if(oldItem.isPresent()
                && (!oldItem.get().getCode().equalsIgnoreCase(itemDTO.getCode()) || !oldItem.get().getClient().getId().equals(client.get().getId()))){
                if (itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId()).isPresent()) {
                    throw new TbsRunTimeException(languageUtil.getMessageByKey("item.already.created", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
                }
            }
        }

        item.setClient(client.get());

        Item persistedItem = itemRepository.save(item);
        saveAudit(persistedItem);
        ItemDTO itemResultDTO = itemMapper.toDto(persistedItem);
        return itemResultDTO;
    }


    public ItemDTO updateItemByApp(ItemDTO itemDTO, String language) {

        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        if (!client.isPresent()) {
            throw new TbsRunTimeException(languageUtil.getMessageByKey("client.not.authorized", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
        }
        Optional<Item> item = itemRepository.findByCodeAndClientId(itemDTO.getCode(), client.get().getId());
        if (!item.isPresent()) {
            throw new TbsRunTimeException(languageUtil.getMessageByKey("item.not.found", Constants.LANGUAGE.getLanguageByHeaderKey(language)));

        }
        // update tax
        Set<Tax> taxes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(itemDTO.getTaxes())) {
            for(TaxDTO taxDTO : itemDTO.getTaxes()){
                Optional<Tax> tax = taxRepository.findByCode(taxDTO.getCode());
                if (tax.isPresent()) {
                    taxes.add(tax.get());
                } else {
                    throw new TbsRunTimeException(languageUtil.getMessageByKey("tax.does.not.exist", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
                }
            }
        }
        item.get().setTaxes(taxes);
        // update Category
        if(itemDTO.getCategory() != null && StringUtils.isNotEmpty(itemDTO.getCategory().getCode())) {
            // set category and client
            Optional<Category> category = categoryRepository.findByCode(itemDTO.getCategory().getCode());
            if (!category.isPresent()) {
                throw new TbsRunTimeException(languageUtil.getMessageByKey("category.does.not.exist", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
            }
            item.get().setCategory(category.get());
        }
        // update price
        if (!itemDTO.isFlexiblePrice() && itemDTO.getPrice() != null) {
            item.get().setPrice(itemDTO.getPrice());
        } else if(itemDTO.isFlexiblePrice()) {
            item.get().setPrice(BigDecimal.ZERO);
        }
        item.get().setFlexiblePrice(itemDTO.isFlexiblePrice());
        if (!itemDTO.isFlexiblePrice() && (itemDTO.getPrice() == null || itemDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new TbsRunTimeException(languageUtil.getMessageByKey("wrong.price", Constants.LANGUAGE.getLanguageByHeaderKey(language)));
        }
        // update name
        if (StringUtils.isNotEmpty(itemDTO.getName())) {
            item.get().setName(itemDTO.getName());
        }
        // update default quantity
        if (itemDTO.getDefaultQuantity() != null) {
            item.get().setDefaultQuantity(itemDTO.getDefaultQuantity());
        }
        Item persistedItem = itemRepository.save(item.get());
        saveAudit(persistedItem);
        ItemDTO itemResultDTO = itemMapper.toDto(persistedItem);
        return itemResultDTO;
    }

    private void saveAudit(Item item) {
        ItemHistory itemHistory = itemHistoryMapper.itemToEntity(item);
        BigDecimal totalTaxes = item.getTaxes().stream().map(tax -> tax.getRate()).reduce(BigDecimal.ZERO, BigDecimal::add);
        itemHistory.setTotalTaxes(totalTaxes);
        itemHistory.setLastModifiedBy(SecurityUtils.getCurrentUserLogin().orElse(""));
        itemHistory.setLastModifiedDate(ZonedDateTime.now());
        itemHistoryRepository.save(itemHistory);
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
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<ItemDTO> findAllByClient() {
        log.debug("Request to get all Items by client");
        String appName = SecurityUtils.getCurrentUserLogin().orElse("");
        Optional<Client> client =  clientService.getClientByClientId(appName);
        return itemRepository.findByClientId(client.get().getId()).stream()
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

    public List<ItemHistoryDTO> findItemHistory(Long itemId) {
        return itemHistoryMapper.toDto(itemHistoryRepository.findByItemId(itemId));
    }
}
