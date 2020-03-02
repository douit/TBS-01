package sa.tamkeentech.tbs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.domain.enumeration.IdentityType;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;
import sa.tamkeentech.tbs.repository.FileSyncLogRepository;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.dto.RefundDTO;
import sa.tamkeentech.tbs.service.dto.RefundDetailedDTO;
import sa.tamkeentech.tbs.service.dto.RefundStatusCCResponseDTO;
import sa.tamkeentech.tbs.service.dto.TBSEventReqDTO;
import sa.tamkeentech.tbs.service.mapper.RefundMapper;
import sa.tamkeentech.tbs.service.util.EventPublisherService;
import sa.tamkeentech.tbs.web.rest.errors.ErrorConstants;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.persistence.criteria.Predicate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
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
    private final FileSyncLogRepository fileSyncLogRepository;
    @Value("${tbs.refund.sadad-url-refund}")
    private String sadadUrlRefund;
    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;
    @Value("${tbs.payment.credit-card-biller-code}")
    private String billerCode;
    @Value("${tbs.payment.credit-card-refund-url}")
    private String creditCardRefundUrl;
    @Value("${tbs.refund.sadad-shared-folder}")
    private String sadadSharedFolder;
    @Value("${tbs.payment.sts-secret-key}")
    private String stsSecretKey;
    @Value("${tbs.payment.sts-post-form-url}")
    private String stsPostFormUrl;
    @Value("${tbs.payment.sts-refund-status}")
    private String stsCheckStatusUrl;
    @Value("${tbs.payment.sts-merchant-id}")
    private String stsMerchantId;
    @Autowired
    @Lazy
    EventPublisherService eventPublisherService;

    @Autowired
    private UserService userService;

    public RefundService(RefundRepository refundRepository, RefundMapper refundMapper, ObjectMapper objectMapper, PaymentRepository paymentRepository, InvoiceRepository invoiceResitory, FileSyncLogRepository fileSyncLogRepository) {
        this.refundRepository = refundRepository;
        this.refundMapper = refundMapper;
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
        this.invoiceResitory = invoiceResitory;
        this.fileSyncLogRepository = fileSyncLogRepository;
    }

    /**
     * Create new refund.
     *
     * @param refundDTO the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public RefundDTO createNewRefundAndSendEvent(RefundDTO refundDTO) throws IOException {
        log.debug("Request new Refund : {}", refundDTO);
        if (refundDTO.getAccountId() == null) {
            throw new TbsRunTimeException("Invoice Id is mandatory to process the refund");
        }
        Optional<Payment> payment = paymentRepository.findFirstByInvoiceAccountIdAndStatus(refundDTO.getAccountId(), PaymentStatus.PAID);
        // check if there is a payment
        if (!payment.isPresent()) {
            throw new TbsRunTimeException("No successful payment for invoice or invoice already refunded: " + refundDTO.getAccountId().toString());
        }
        // check if there is a previous refund
        for (Refund refund : payment.get().getRefunds()) {
            if (refund.getStatus() == RequestStatus.PENDING || refund.getStatus() == RequestStatus.SUCCEEDED) {
                throw new TbsRunTimeException("There is an existing refund for invoice: " + refundDTO.getAccountId() + " with status: " + refund.getStatus().name());
            }
        }
        Invoice invoice = payment.get().getInvoice();


        TBSEventReqDTO<RefundDTO> reqNotification = TBSEventReqDTO.<RefundDTO>builder()
            .principalId(invoice.getCustomer().getIdentity()).referenceId(invoice.getAccountId().toString())
            .req(refundDTO).build();
        RefundDTO resp = eventPublisherService.createNewRefund(reqNotification, invoice, payment).getResp();
        return resp;
    }


    public RefundDTO createNewRefund(RefundDTO refundDTO, Invoice invoice, Optional<Payment> payment) throws IOException {

        Refund refund = refundMapper.toEntity(refundDTO);
        refund.setStatus(RequestStatus.CREATED);
        refund.setPayment(payment.get());
        refund = refundRepository.save(refund);
        if (payment.get().getPaymentMethod().getCode().equalsIgnoreCase(Constants.SADAD)) {
            int sadadResult;
            try {
                sadadResult = sendEventAndCallRefundBySdad(refund, invoice);
            } catch (IOException | JSONException e) {
                // ToDo add new exception 500 for sadad
                throw new PaymentGatewayException("Sadad issue");
            }
            // ToDo add new exception 500 for sadad
            // invoice = invoiceRepository.getOne(invoice.getId());
            if (sadadResult != 200) {
                refund.setStatus(RequestStatus.FAILED);
                throw new PaymentGatewayException("Sadad refund creation error");
            } else {
                refund.setStatus(RequestStatus.PENDING);
                payment.get().setStatus(PaymentStatus.REFUNDED);
                invoice.setPaymentStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment.get());
                invoiceResitory.save(invoice);
            }
        } else {
            // RefundStatusCCResponseDTO refundResponseDTO = callRefundByCreditCard(refundDTO, refund.getId(), invoice.getId(), invoice.getClient().getPaymentKeyApp());
            int returnCode = callRefundByCreditCardAndSendEvent(refund, payment.get().getTransactionId(), invoice);
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


    public int sendEventAndCallRefundBySdad(Refund refund, Invoice invoice) throws IOException, JSONException {
        JSONObject refundInfo = new JSONObject();
        // ToDo check if refundId must be unique per app ? other params ...
        Customer customer = invoice.getCustomer();
        refundInfo.put("refundId", refund.getId());
        refundInfo.put("customerId", customer.getIdentity());
        refundInfo.put("customerIdType", (customer.getIdentityType() != null) ? customer.getIdentityType().name() : IdentityType.NAT.name());
        refundInfo.put("amount", refund.getPayment().getAmount());
        refundInfo.put("paymetTransactionId", refund.getPayment().getTransactionId());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 30);
        String expiryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
        // ExpiryDate of the invoice, not related to invoice
        refundInfo.put("expirydate", expiryDate);
        refundInfo.put("bankId", refund.getPayment().getBankId());
        refundInfo.put("applicationId", sadadApplicationId);

        TBSEventReqDTO<String> req = TBSEventReqDTO.<String>builder()
            .principalId(customer.getIdentity()).referenceId(invoice.getAccountId().toString()).req(refundInfo.toString()).build();
        return eventPublisherService.callSadadRefundEvent(req).getResp();

    }

    public int callRefundBySdad(String req) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrlRefund);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(req));
        HttpResponse response;
        response = client.execute(post);

        log.debug("----Sadad refund request : {}", req);
        log.info("----Sadad refund response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad refund response content : {}", response.getEntity().getContent());
            log.debug("----Sadad refund response entity : {}", response.getEntity().toString());
        }
        return response.getStatusLine().getStatusCode();

    }

    int callRefundByCreditCardAndSendEvent(Refund refund, String transactionId, Invoice invoice) throws IOException {
        //Step 1: Generate Secure Hash
      //  String SECRET_KEY = "Y2FkMTdlOWZiMzJjMzY4ZGFkMzhkMWIz"; // Use Yours, Please Store Your Secret Key in safe Place(e.g. database)

        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String, String> parameters = new TreeMap<String, String>();

        // fill required parameters
        parameters.put("MessageID", "4");
        parameters.put("TransactionID", refund.getId().toString());
        parameters.put("OriginalTransactionID", transactionId);
        parameters.put("MerchantID", stsMerchantId);
        BigDecimal roundedAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
        String formattedAmount = roundedAmount.multiply(new BigDecimal("100")).toBigInteger().toString();
        parameters.put("Amount", formattedAmount);
//        invoice.getAmount().toString()
        parameters.put("CurrencyISOCode", "682");
        parameters.put("Version", "1.0");

        //Create an Ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(stsSecretKey);
        for (String treeMapKey : parameters.keySet()) {
            orderedString.append(parameters.get(treeMapKey));
        }
        System.out.println("orderdString " + orderedString);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        StringBuffer requestQuery = new StringBuffer();
        requestQuery.append("TransactionID").append("=").append(refund.getId()).append("&").
            append("MerchantID").append("=").append(stsMerchantId).append("&").
            append("MessageID").append("=").append("4").append("&").
            append("Amount").append("=").append(formattedAmount).append("&")
            .append("OriginalTransactionID").append("=").append(transactionId).append("&")
            .append("CurrencyISOCode").append("=").append("682").append("&")
            .append("SecureHash").append("=").append(secureHash).append("&")
            .append("Version").append("=").append("1.0").append("&");

        //Send the request
        URL url = new URL(stsCheckStatusUrl);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

        //write parameters
        writer.write(requestQuery.toString());
        writer.flush();

        // Get the response
        StringBuffer output = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        writer.close();
        reader.close();

        //Output the response
        System.out.println(output.toString());

        // this string is formatted as a "Query String" - name=value&name2=value2.......
        String outputString = output.toString();

        // To read the output string you might want to split it
        // on '&' to get pairs then on '=' to get name and value
        // and for a better and ease on verifying secure hash you should put them in a TreeMap
        String[] pairs = outputString.split("&");
        Map<String, String> result = new TreeMap<String, String>();

        // now we have separated the pairs from each other {"name1=value1","name2=value2",....}
        for (String pair : pairs) {
            // now we have separated the pair to {"name”, “value"}
            String[] nameValue = pair.split("=");
            String name = nameValue[0];//first element is the name
            String value = nameValue[1];//second element is the value
            // put the pair in the result map
            result.put(name, value);
        }

        // Now that we have the map, order it to generate secure hash and compare it with the received one
        StringBuilder responseOrderdString = new StringBuilder();
        responseOrderdString.append(stsSecretKey);
        for (String treeMapKey : result.keySet()) {
            if (!treeMapKey.equals("Response.SecureHash")) {
                responseOrderdString.append(result.get(treeMapKey));
            }
        }

        String formattedResponse = responseOrderdString.toString().replaceAll(" ", "+");
        log.debug("Response Orderd String is " + formattedResponse);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(formattedResponse).getBytes());
        // get the received secure hash from result map
        String receivedSecurehash = result.get("Response.SecureHash");
        if (!receivedSecurehash.equals(generatedsecureHash)) {
            //IF they are not equal then the response shall not be accepted
            throw new TbsRunTimeException("Received Secure Hash does not Equal generated Secure hash");
        } else {
            // Complete the Action get other parameters from result map and do your processes // please refer to The Integration Manual to See The List of The Received Parameters
            String status = result.get("Response.StatusCode");
            if (Constants.CC_PAYMENT_SUCCESS_CODE.equals(status)) {
                log.debug("Successful refund {}", invoice.getAccountId());
                return 200;
            }
        }
        return 1;


//        HttpClient client = HttpClientBuilder.create().build();
//        HttpPost post = new HttpPost(creditCardRefundUrl);
//        post.setHeader("Content-Type", "application/json");
//        JSONObject billInfoContent = new JSONObject();
//        RefundStatusCCResponseDTO refundResponseDTO = null;
//        try {
//            Calendar rightNowDate = Calendar.getInstance();
//            billInfoContent.put("OriginalTransactionID", /*invoiceId*/ transactionId);
//            billInfoContent.put("TransactionId",invoice.getAccountId().toString() + rightNowDate.get(Calendar.MINUTE) + rightNowDate.get(Calendar.SECOND));
//            billInfoContent.put("BillerCode", billerCode);
//            billInfoContent.put("AppCode", invoice.getClient().getPaymentKeyApp());
//            billInfoContent.put("Amount", refund.getAmount());
//            String jsonStr = billInfoContent.toString();
//            log.debug("++++Refund CC request : {}", jsonStr);
//
//            TBSEventReqDTO<String> req = TBSEventReqDTO.<String>builder()
//                .principalId(invoice.getCustomer().getIdentity()).referenceId(invoice.getAccountId().toString()).req(jsonStr).build();
//            return eventPublisherService.callRefundByCreditCardEvent(req).getResp();
//        } catch (JSONException | IOException e) {
//            log.error("Payment gateway issue: {}", e.getCause());
//        }
//
//        //return refundResponseDTO;
//        return HttpStatus.EXPECTATION_FAILED.value();
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

    public Boolean updateSadadRefundAndSendEvent(RefundDTO refundDTO) {
        TBSEventReqDTO<RefundDTO> reqNotification = TBSEventReqDTO.<RefundDTO>builder().principalId(refundDTO.getCustomerId())
            .referenceId(refundDTO.getAccountId().toString()).req(refundDTO).build();
        return eventPublisherService.updateSadadRefund(reqNotification).getResp();
    }

    public Boolean updateSadadRefund(Long id) {
        if (refundRepository.setStatus(id, RequestStatus.SUCCEEDED) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Sync Sadad refunds every hour.
     * <p>
     * This is scheduled to get fired every hour, at 01:00 - 02:00 ...
     */
    @Scheduled(cron = "${tbs.cron.refund-sadad-sync}")
    public void syncSadadRefund() {
        log.info("----Start Sadad Refund Sync");
        boolean successful = true;
        File directory = new File(sadadSharedFolder);
        Calendar currentDate = Calendar.getInstance();
        String currentDateFormatted = new SimpleDateFormat("yyyy-MM-dd").format(currentDate.getTime());

        Calendar previousDay = Calendar.getInstance();
        previousDay.add(Calendar.DATE, -1);
        String previousDayFormatted = new SimpleDateFormat("yyyy-MM-dd").format(previousDay.getTime());

        String filePrefixCurrentDay = new StringBuilder("blrrrq-").append(currentDateFormatted).toString();
        String filePrefixPreviousDayDay = new StringBuilder("blrrrq-").append(previousDayFormatted).toString();

        File[] files = directory.listFiles((File dir, String name) -> {
            String lowercaseName = name.toLowerCase();
            if ((lowercaseName.startsWith(filePrefixCurrentDay) || lowercaseName.startsWith(filePrefixPreviousDayDay))
                && lowercaseName.endsWith(".xml")) {
                if (!fileSyncLogRepository.findByFileName(name).isPresent()) {
                    return true;
                }
            }
            return false;
        });
        if (files == null) {
            return;
        }
        for (File file : files) {
            Date lastMod = new Date(file.lastModified());
            // ZonedDateTime lastModZoneDate = ZonedDateTime.ofInstant(lastMod.toInstant(), ZoneId.of("Asia/Riyadh"));
            ZonedDateTime lastModZoneDate = ZonedDateTime.ofInstant(lastMod.toInstant(), ZoneId.systemDefault());
            log.info("----Processing Sadad File: " + file.getName() + ", Date: " + lastModZoneDate);
            int totalRefund = 0;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                NodeList nodeList = document.getDocumentElement().getElementsByTagName("ReconRefundRec");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node refundRecordNode = nodeList.item(i);

                    // RefundStatus node
                    Node refundStatusNode = ((Element) refundRecordNode).getElementsByTagName("RefundStatus").item(0);
                    // Get the value of RefundStatusCode.
                    String status = ((Element) refundStatusNode).getElementsByTagName("RefundStatusCode")
                        .item(0).getChildNodes().item(0).getNodeValue();
                    if (status.equalsIgnoreCase("reconciled")) {
                        // ReconRefundInfo node
                        Node refundInfoNode = ((Element) refundRecordNode).getElementsByTagName("ReconRefundInfo").item(0);
                        String refundId = ((Element) refundInfoNode).getElementsByTagName("RefundId")
                            .item(0).getChildNodes().item(0).getNodeValue();
                        Optional<Refund> refund = refundRepository.findById(Long.parseLong(refundId));
                        if (refund.isPresent() && refund.get().getStatus() == RequestStatus.PENDING) {
                            RefundDTO refundDTO = refundMapper.toDto(refund.get());
                            updateSadadRefundAndSendEvent(refundDTO);
                            log.info("++++++ Refund {} is reconciled and updated", refundId);
                            totalRefund++;
                        } else {
                            log.info("------ Refund {} is reconciled and ignored", refundId);
                        }

                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                successful = false;
                e.printStackTrace();
            }
            FileSyncLog fileSyncLog = FileSyncLog.builder()
                .fileName(file.getName())
                .dateCreated(lastModZoneDate)
                .dateExecuted(ZonedDateTime.now())
                .type("SADAD_REFUND")
                .totalReconciled(totalRefund)
                .successful(successful).build();
            fileSyncLogRepository.save(fileSyncLog);
            log.info("Finish Processing Sadad File: " + file.getName() + ", TotalRefund: " + totalRefund);
        }
    }

    // payment report data
    List<RefundDetailedDTO> getRefundsBetween(ZonedDateTime start, ZonedDateTime end, Long clientId) {
        List<Long> clientIds = userService.listClientIds(clientId);

        return refundMapper.toDetailedDto(refundRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.and(root.get("payment").get("invoice").get("client").get("id").in(clientIds)));
            // predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), PaymentStatus.PAID)));
            if (start != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), start)));
            }
            if (end != null) {
                predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), end)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }));
    }

}
