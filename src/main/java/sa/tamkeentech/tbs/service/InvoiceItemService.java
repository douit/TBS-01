package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.InvoiceItem;
import sa.tamkeentech.tbs.repository.InvoiceItemRepository;
import sa.tamkeentech.tbs.service.InvoiceItemService;
import sa.tamkeentech.tbs.service.dto.InvoiceItemDTO;
import sa.tamkeentech.tbs.service.mapper.InvoiceItemMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link InvoiceItem}.
 */
@Service
@Transactional
public class InvoiceItemService {

    private final Logger log = LoggerFactory.getLogger(InvoiceItemService.class);

    private final InvoiceItemRepository invoiceItemRepository;

    private final InvoiceItemMapper invoiceItemMapper;

    public InvoiceItemService(InvoiceItemRepository invoiceItemRepository, InvoiceItemMapper invoiceItemMapper) {
        this.invoiceItemRepository = invoiceItemRepository;
        this.invoiceItemMapper = invoiceItemMapper;
    }

    /**
     * Save a invoiceItem.
     *
     * @param invoiceItemDTO the entity to save.
     * @return the persisted entity.
     */
    public InvoiceItemDTO save(InvoiceItemDTO invoiceItemDTO) {
        log.debug("Request to save InvoiceItem : {}", invoiceItemDTO);
        InvoiceItem invoiceItem = invoiceItemMapper.toEntity(invoiceItemDTO);
        invoiceItem = invoiceItemRepository.save(invoiceItem);
        return invoiceItemMapper.toDto(invoiceItem);
    }

    /**
     * Get all the invoiceItems.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<InvoiceItemDTO> findAll() {
        log.debug("Request to get all InvoiceItems");
        return invoiceItemRepository.findAll().stream()
            .map(invoiceItemMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one invoiceItem by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InvoiceItemDTO> findOne(Long id) {
        log.debug("Request to get InvoiceItem : {}", id);
        return invoiceItemRepository.findById(id)
            .map(invoiceItemMapper::toDto);
    }

    /**
     * Delete the invoiceItem by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete InvoiceItem : {}", id);
        invoiceItemRepository.deleteById(id);
    }
}
