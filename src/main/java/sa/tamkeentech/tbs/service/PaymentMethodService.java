package sa.tamkeentech.tbs.service;

import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.repository.PaymentMethodRepository;
import sa.tamkeentech.tbs.service.dto.PaymentMethodDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMethodMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link PaymentMethod}.
 */
@Service
@Transactional
public class PaymentMethodService {

    private final Logger log = LoggerFactory.getLogger(PaymentMethodService.class);

    private final PaymentMethodRepository paymentMethodRepository;

    private final PaymentMethodMapper paymentMethodMapper;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository, PaymentMethodMapper paymentMethodMapper) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentMethodMapper = paymentMethodMapper;
    }

    /**
     * Save a paymentMethod.
     *
     * @param paymentMethodDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentMethodDTO save(PaymentMethodDTO paymentMethodDTO) {
        log.debug("Request to save PaymentMethod : {}", paymentMethodDTO);
        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(paymentMethodDTO);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        return paymentMethodMapper.toDto(paymentMethod);
    }

    /**
     * Get all the paymentMethods.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> findAll() {
        log.debug("Request to get all PaymentMethods");
        return paymentMethodRepository.findAll().stream()
            .map(paymentMethodMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one paymentMethod by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentMethodDTO> findOne(Long id) {
        log.debug("Request to get PaymentMethod : {}", id);
        return paymentMethodRepository.findById(id)
            .map(paymentMethodMapper::toDto);
    }

    /**
     * Delete the paymentMethod by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete PaymentMethod : {}", id);
        paymentMethodRepository.deleteById(id);
    }

    public Optional<PaymentMethod> findById(Long id)  {
        return paymentMethodRepository.findById(id);
    }

    public Optional<PaymentMethod> findByCode(String code)  {
        return paymentMethodRepository.findByCode(code);
    }
}
