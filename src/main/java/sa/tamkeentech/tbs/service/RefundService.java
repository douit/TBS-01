package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.dto.RefundDTO;
import sa.tamkeentech.tbs.service.dto.RefundResponseDTO;
import sa.tamkeentech.tbs.service.dto.RefundStatusCCResponseDTO;
import sa.tamkeentech.tbs.service.dto.TBSEventReqDTO;
import sa.tamkeentech.tbs.service.mapper.RefundMapper;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

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
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceResitory;
    private final RefundMapper refundMapper;
    @Value("${tbs.refund.sadad-url-refund}")
    private String  sadadUrlRefund;
    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;
    @Value("${tbs.payment.credit-card-biller-code}")
    private String billerCode;
    @Value("${tbs.payment.credit-card-refund-url}")
    private String creditCardRefundUrl;

    @Autowired
    @Lazy
    EventPublisherService eventPublisherService;

    public RefundService(RefundRepository refundRepository, RefundMapper refundMapper, ObjectMapper objectMapper, PaymentRepository paymentRepository, InvoiceRepository invoiceResitory) {
        this.refundRepository = refundRepository;
        this.refundMapper = refundMapper;
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.invoiceResitory = invoiceResitory;
    }
    /**
     * Create new refund.
     *
     * @param refundDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public RefundDTO createNewRefundAndSendEvent(RefundDTO refundDTO) {
        log.debug("Request new Refund : {}", refundDTO);
        if (refundDTO.getInvoiceId() == null) {
            throw new TbsRunTimeException("Invoice Id is mandatory to process the refund");
        }
        Optional<Payment> payment = paymentRepository.findFirstByInvoiceIdAndStatus(refundDTO.getInvoiceId(), PaymentStatus.PAID);
        Invoice invoice = payment.get().getInvoice();


        TBSEventReqDTO<RefundDTO> reqNotification = TBSEventReqDTO.<RefundDTO>builder()
            .principalId(invoice.getCustomer().getIdentity()).referenceId(invoice.getAccountId().toString())
            .req(refundDTO).build();
        RefundDTO resp = eventPublisherService.createNewRefund(refundDTO, invoice, payment).getResp();
        return resp;
    }


    public RefundDTO createNewRefund(RefundDTO refundDTO, Invoice invoice, Optional<Payment> payment) {
        if (!payment.isPresent()) {
            throw new TbsRunTimeException("No successful payment for invoice or invoice already refunded: "+ refundDTO.getInvoiceId().toString());
        }
        Refund refund = refundMapper.toEntity(refundDTO);
        refund.setStatus(RequestStatus.CREATED);
        refund.setPayment(payment.get());
        refund = refundRepository.save(refund);
        if (payment.get().getPaymentMethod().getCode().equalsIgnoreCase(Constants.SADAD)) {
            int sadadResult;
            try {
                sadadResult = callRefundBySdad(refundDTO, refund.getId());
            } catch (IOException | JSONException e) {
                // ToDo add new exception 500 for sadad
                throw new TbsRunTimeException("Sadad issue", e);
            }
            // ToDo add new exception 500 for sadad
            // invoice = invoiceRepository.getOne(invoice.getId());
            if (sadadResult != 200) {
                refund.setStatus(RequestStatus.FAILED);
                throw new TbsRunTimeException("Sadad refund creation error");
            } else {
                refund.setStatus(RequestStatus.CREATED);
            }
        } else {
            // RefundStatusCCResponseDTO refundResponseDTO = callRefundByCreditCard(refundDTO, refund.getId(), invoice.getId(), invoice.getClient().getPaymentKeyApp());
            int returnCode = callRefundByCreditCardAndSendEvent(refundDTO, payment.get().getTransactionId(), invoice);
            // if (refundResponseDTO != null && Constants.CC_REFUND_SUCCESS_CODE.equals(refundResponseDTO.getCode())) {
            if (returnCode == 200) {
                refund.setStatus(RequestStatus.SUCCEEDED);
                payment.get().setStatus(PaymentStatus.REFUNDED);
                invoice.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment.get());
                invoiceResitory.save(invoice);
            } else {
                refund.setStatus(RequestStatus.FAILED);
            }
        }
        refund = refundRepository.save(refund);


        RefundDTO result = refundMapper.toDto(refund);
        return result;
    }


    public int callRefundBySdad( RefundDTO refund, Long refundId) throws IOException,JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrlRefund);
        post.setHeader("Content-Type", "application/json");
        RefundResponseDTO refundResponseDTO = null;
        JSONObject RefundInfo = new JSONObject();
        // ToDo check if refundId must be unique per app ? other params ...
        RefundInfo.put("refundId", refundId);
        // RefundInfo.put("customerId", refund.getCustomerId());
        // RefundInfo.put("customerIdType", refund.getCustomerIdType());
        RefundInfo.put("amount", refund.getAmount());
        // RefundInfo.put("paymetTransactionId", refund.getPaymetTransactionId());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 2);
        String expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        // ToDo is it the expiryDate of the invoice?
        RefundInfo.put("expirydate",expiryDate);
        // How to get bankId
        // RefundInfo.put("bankId", refund.getBankId());
        RefundInfo.put("applicationId", sadadApplicationId);
        String jsonStr = RefundInfo.toString();
        post.setEntity(new StringEntity(jsonStr));
        HttpResponse response;
        response = client.execute(post);

        log.debug("----Sadad request : {}", jsonStr);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response content : {}", response.getEntity().getContent());
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }
        return response.getStatusLine().getStatusCode();

    }

    int callRefundByCreditCardAndSendEvent(RefundDTO refund, String transactionId, Invoice invoice) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(creditCardRefundUrl);
        post.setHeader("Content-Type", "application/json");
        JSONObject billInfoContent = new JSONObject();
        RefundStatusCCResponseDTO refundResponseDTO = null;
        try {
            Calendar rightNowDate = Calendar.getInstance();
            billInfoContent.put("OriginalTransactionID", /*invoiceId*/ transactionId);
            billInfoContent.put("TransactionId",invoice.getAccountId().toString() + rightNowDate.get(Calendar.MINUTE) + rightNowDate.get(Calendar.SECOND));
            billInfoContent.put("BillerCode", billerCode);
            billInfoContent.put("AppCode", invoice.getClient().getPaymentKeyApp());
            billInfoContent.put("Amount", refund.getAmount());
            String jsonStr = billInfoContent.toString();
            log.debug("++++Refund CC request : {}", jsonStr);

            TBSEventReqDTO<String> req = TBSEventReqDTO.<String>builder()
                .principalId(invoice.getCustomer().getIdentity()).referenceId(invoice.getAccountId().toString()).req(jsonStr).build();
            return eventPublisherService.callRefundByCreditCardEvent(req).getResp();
        } catch (JSONException | IOException e) {
            log.error("Payment gateway issue: {}", e.getCause());
        }

        //return refundResponseDTO;
        return HttpStatus.EXPECTATION_FAILED.value();
    }

    public Integer callRefundByCreditCard(String jsonStr) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(creditCardRefundUrl);
        post.setHeader("Content-Type", "application/json");
        RefundStatusCCResponseDTO refundResponseDTO = null;
        post.setEntity(new StringEntity(jsonStr));
        log.debug("++++Refund CC request : {}", jsonStr);
        HttpResponse response = client.execute(post);
        if (response.getEntity() != null) {
            log.debug("----Refund CC response content : {}", response.getEntity().getContent().toString());
            log.debug("----Refund CC response Code : {}", response.getStatusLine().getStatusCode());
        }
        refundResponseDTO = objectMapper.readValue(response.getEntity().getContent(), RefundStatusCCResponseDTO.class);
        log.info("************** response from credit Card ************ : " + refundResponseDTO);
        return response.getStatusLine().getStatusCode();
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
