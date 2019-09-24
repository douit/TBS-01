package sa.tamkeentech.tbs.service;

import sa.tamkeentech.tbs.domain.Discount;
import sa.tamkeentech.tbs.repository.DiscountRepository;
import sa.tamkeentech.tbs.service.dto.DiscountDTO;
import sa.tamkeentech.tbs.service.mapper.DiscountMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Discount}.
 */
@Service
@Transactional
public class DiscountService {

    private final Logger log = LoggerFactory.getLogger(DiscountService.class);

    private final DiscountRepository discountRepository;

    private final DiscountMapper discountMapper;

    public DiscountService(DiscountRepository discountRepository, DiscountMapper discountMapper) {
        this.discountRepository = discountRepository;
        this.discountMapper = discountMapper;
    }

    /**
     * Save a discount.
     *
     * @param discountDTO the entity to save.
     * @return the persisted entity.
     */
    public DiscountDTO save(DiscountDTO discountDTO) {
        log.debug("Request to save Discount : {}", discountDTO);
        Discount discount = discountMapper.toEntity(discountDTO);
        discount = discountRepository.save(discount);
        return discountMapper.toDto(discount);
    }

    /**
     * Get all the discounts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DiscountDTO> findAll() {
        log.debug("Request to get all Discounts");
        return discountRepository.findAll().stream()
            .map(discountMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one discount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DiscountDTO> findOne(Long id) {
        log.debug("Request to get Discount : {}", id);
        return discountRepository.findById(id)
            .map(discountMapper::toDto);
    }

    /**
     * Delete the discount by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Discount : {}", id);
        discountRepository.deleteById(id);
    }
}
