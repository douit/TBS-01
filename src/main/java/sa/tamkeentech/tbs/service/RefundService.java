package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.service.dto.RefundDTO;
import sa.tamkeentech.tbs.service.dto.RefundResponseDTO;
import sa.tamkeentech.tbs.service.mapper.RefundMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Refund}.
 */
@Service
@Transactional
public class RefundService {

    private final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final RefundRepository refundRepository;
    private final ObjectMapper objectMapper;

    private final RefundMapper refundMapper;
    @Value("${tbs.refund.sadad-url-refund}")
    private String  sadadUrlRefund;

    public RefundService(RefundRepository refundRepository, RefundMapper refundMapper,ObjectMapper objectMapper) {
        this.refundRepository = refundRepository;
        this.refundMapper = refundMapper;
        this.objectMapper = objectMapper;
    }
    /**
     * Create new refund.
     *
     * @param refundDTO the entity to save.
     * @return the persisted entity.
     */
    public RefundDTO createNewRefund(RefundDTO refundDTO) throws IOException, JSONException {
        log.debug("Request to save Refund : {}", refundDTO);
        Refund refund = refundMapper.toEntity(refundDTO);
        refund.setOfficialId(refundDTO.getCustomerId());
        RefundResponseDTO refundResponseDTO = callRefundBySdad(refundDTO);
        refund.setBillerId(refundResponseDTO.getBillerId());
        if(refundResponseDTO.getStatus().getStatusCode() == 0){
            refund.setStatus(PaymentStatus.PAID);
        }else{
            refund.setStatus(PaymentStatus.PENDING);
        }
        refund = refundRepository.save(refund);
        RefundDTO result = refundMapper.toDto(refund);
        return result;
    }

    private RefundResponseDTO callRefundBySdad( RefundDTO refund) throws IOException,JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrlRefund);
        post.setHeader("Content-Type", "application/json");
        RefundResponseDTO refundResponseDTO = null;
        JSONObject RefundInfo = new JSONObject();
        RefundInfo.put("refundId", refund.getRefundId());
        RefundInfo.put("customerId", refund.getCustomerId());
        RefundInfo.put("customerIdType", refund.getCustomerIdType());
        RefundInfo.put("amount", refund.getAmount());
        RefundInfo.put("paymetTransactionId", refund.getPaymetTransactionId());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 2);
        String expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        RefundInfo.put("expirydate",expiryDate);
        RefundInfo.put("bankId", refund.getBankId());
        RefundInfo.put("applicationId", refund.getApplicationId());
        String jsonStr = RefundInfo.toString();
        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response;
        response = client.execute(post);
        refundResponseDTO = objectMapper.readValue(response.getEntity().getContent(), RefundResponseDTO.class);

        log.debug("----Sadad request : {}", jsonStr);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response content : {}", response.getEntity().getContent());
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }
        return  refundResponseDTO;

    }

    /**
         * Save a refund.
         *
         * @param refundDTO the entity to save.
         * @return the persisted entity.
         */
    public RefundDTO save(RefundDTO refundDTO) {
        log.debug("Request to save Refund : {}", refundDTO);
        Refund refund = refundMapper.toEntity(refundDTO);
        refund = refundRepository.save(refund);
        return refundMapper.toDto(refund);
    }

    /**
     * Get all the refunds.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<RefundDTO> findAll() {
        log.debug("Request to get all Refunds");
        return refundRepository.findAll().stream()
            .map(refundMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one refund by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RefundDTO> findOne(Long id) {
        log.debug("Request to get Refund : {}", id);
        return refundRepository.findById(id)
            .map(refundMapper::toDto);
    }

    /**
     * Delete the refund by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Refund : {}", id);
        refundRepository.deleteById(id);
    }
}
