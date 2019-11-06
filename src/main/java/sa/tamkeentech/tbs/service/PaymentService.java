package sa.tamkeentech.tbs.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link Payment}.
 */
@Service
@Transactional
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    @Value("${tbs.payment.sadad-account-prefix}")
    private String sadadAccountPrefix;

    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;

    @Value("${tbs.payment.credit-card-url}")
    private String creditCardUrl;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Save a payment.
     *
     * @param paymentDTO the entity to save.
     * @return the persisted entity.
     */
    public PaymentDTO save(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    /**
     * Get all the payments.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> findAll() {
        log.debug("Request to get all Payments");
        return paymentRepository.findAll().stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one payment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PaymentDTO> findOne(Long id) {
        log.debug("Request to get Payment : {}", id);
        return paymentRepository.findById(id)
            .map(paymentMapper::toDto);
    }

    /**
     * Delete the payment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Payment : {}", id);
        paymentRepository.deleteById(id);
    }


    public int sadadCall(Long sadadBillId, String sadadAccount , BigDecimal amount) throws IOException, JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrl);
        post.setHeader("Content-Type", "application/json");
        //JSONObject accountInfo = new JSONObject();

        JSONObject billInfo = new JSONObject();
        JSONObject billInfoContent = new JSONObject();
        billInfoContent.put("billNumber", sadadBillId ); // autoincrement
        billInfoContent.put("billAccount", sadadAccount); // Unique 15 digits
        billInfoContent.put("amount",amount);
        Calendar c1 = Calendar.getInstance();
        // c.add(Calendar.HOUR, 3);
        // ToDO delete +35 work arround !!!!
        c1.add(Calendar.MINUTE, 35);
        String dueDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c1.getTime());
        billInfoContent.put("duedate", dueDate);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 2);
        String expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        billInfoContent.put("expirydate",expiryDate);
        billInfoContent.put("billStatus","BillNew");
        // applicationId 0 for test
        billInfoContent.put("applicationId",sadadApplicationId);
        billInfo.put("BillInfo", billInfoContent);
        String jsonStr = billInfo.toString();
        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response;
        response = client.execute(post);
       /* if (response.getStatusLine().getStatusCode() == 200){
            return true ;
        }
            return false;*/
        log.debug("----Sadad request : {}", jsonStr);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response content : {}", response.getEntity().getContent());
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }

        return response.getStatusLine().getStatusCode();
    }

    public String creditCardCall( String sadadAccount , BigDecimal amount , String url) throws IOException, JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(creditCardUrl);
        post.setHeader("Content-Type", "application/json");
        JSONObject billInfoContent = new JSONObject();
        billInfoContent.put("BillNumber", sadadAccount);
        billInfoContent.put("ResponseBackURL",url);
        billInfoContent.put("Amount",amount);
        String jsonStr = billInfoContent.toString();
        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response;
        response = client.execute(post);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        log.info("************** response from credit Card ************ : " + responseString.substring(8,responseString.indexOf( ',' )));
        return responseString.substring(8,responseString.indexOf( ',' ) - 1) ;
    }

    public Long getSadadBillAccount(Long billId) {
        // no prefix
        //return sadadAccountPrefix.concat(String.format("%010d", new BigInteger(billId)));
        // return (billId + 7000000065l);
        return billId;
        // return String.format("%010d", new BigInteger(account.toString()));
    }

    public Long getSadadBillId(Long billId) {
        // no prefix
        // return sadadAccountPrefix.concat(String.format("%010d", new BigInteger(billId)));
        // return (billId + 4065l);
        return billId;
        // return String.format("%010d", new BigInteger(account.toString()));
    }

}
