package sa.tamkeentech.tbs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Tax;
import sa.tamkeentech.tbs.repository.TaxRepository;
import sa.tamkeentech.tbs.service.TaxService;
import sa.tamkeentech.tbs.service.dto.TaxDTO;
import sa.tamkeentech.tbs.service.mapper.TaxMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Tax}.
 */
@Service
@Transactional
public class TaxService {

    private final Logger log = LoggerFactory.getLogger(TaxService.class);

    private final TaxRepository taxRepository;

    private final TaxMapper taxMapper;

    public TaxService(TaxRepository taxRepository, TaxMapper taxMapper) {
        this.taxRepository = taxRepository;
        this.taxMapper = taxMapper;
    }

    /**
     * Save a tax.
     *
     * @param taxDTO the entity to save.
     * @return the persisted entity.
     */
    public TaxDTO save(TaxDTO taxDTO) {
        log.debug("Request to save Tax : {}", taxDTO);
        Tax tax = taxMapper.toEntity(taxDTO);
        tax = taxRepository.save(tax);
        return taxMapper.toDto(tax);
    }

    /**
     * Get all the taxes.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<TaxDTO> findAll() {
        log.debug("Request to get all Taxes");
        return taxRepository.findAll().stream()
            .map(taxMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one tax by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TaxDTO> findOne(Long id) {
        log.debug("Request to get Tax : {}", id);
        return taxRepository.findById(id)
            .map(taxMapper::toDto);
    }

    /**
     * Delete the tax by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Tax : {}", id);
        taxRepository.deleteById(id);
    }
}
