package sa.tamkeentech.tbs.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.NotifiReqDTO;
import sa.tamkeentech.tbs.service.dto.NotifiRespDTO;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.service.dto.TokenResponseDTO;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private final InvoiceRepository invoiceRepository;

    private final PaymentMethodService paymentMethodService;


    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    @Value("${tbs.payment.sadad-account-prefix}")
    private String sadadAccountPrefix;

    @Value("${tbs.payment.sadad-application-id}")
    private Long sadadApplicationId;

    @Value("${tbs.payment.credit-card-url}")
    private String creditCardUrl;

    public static final Long DIFF_ACCOUNT_BILL_ID = 6999996000l;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
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
        // return billId;
        return billId - DIFF_ACCOUNT_BILL_ID;
        // return String.format("%010d", new BigInteger(account.toString()));
    }

    //  diff = 7000000065l - 4065l =  6999996000


    @Transactional
    public ResponseEntity<NotifiRespDTO> sendPaymentNotification(NotifiReqDTO req, String apiKey, String apiSecret) {
        log.debug("----Sadad Notification : {}", req);
        // Optional<Invoice> invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount())-7000000065l);
        Optional<Invoice> invoice = invoiceRepository.findById(Long.parseLong(req.getBillAccount()));
        NotifiRespDTO resp = NotifiRespDTO.builder().statusId(1).build();
        for (Payment payment : invoice.get().getPayments()) {
            if (payment.getStatus() == PaymentStatus.PAID) {
                log.warn("Payment already received, Exit without updating Client app");
                return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
            }
        }

        Optional<PaymentMethod> paymentMethod = paymentMethodService.findByCode(Constants.SADAD);
        Payment payment = Payment.builder()
            .invoice(invoice.get())
            .status(PaymentStatus.PAID)
            .amount(new BigDecimal(req.getAmount()))
            .paymentMethod(paymentMethod.get())
            //.expirationDate()
            .build();
        paymentRepository.save(payment);

        if (payment.getId() != null) {
            log.info("Successful TBS update bill: {}", req.getBillAccount());
            RestTemplate rt1 = new RestTemplate();
            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map1= new LinkedMultiValueMap<String, String>();
            map1.add("grant_type", "client_credentials");
            map1.add("client_id", "tamkeen-billing-system");
            //map1.add("client_secret", "06f4c17f-5c4a-492a-9a8e-a10eafec66c6"); // staging
            map1.add("client_secret", "076a2d1c-15c6-4abf-80a7-0b181f18d617"); // production
            org.springframework.http.HttpEntity<MultiValueMap<String, String>> request1 = new org.springframework.http.HttpEntity<MultiValueMap<String, String>>(map1, headers1);
            //uri = "https://sso.tamkeen.land/auth/realms/tamkeen/protocol/openid-connect/token"; // staging
            String uri = "https://accounts.wahid.sa/auth/realms/wahid/protocol/openid-connect/token"; // production
            ResponseEntity<TokenResponseDTO> response1 = rt1.postForEntity( uri, request1 , TokenResponseDTO.class );
            // log.info("DVS Token" +  response1.getBody().getAccess_token());
            RestTemplate restTemplate = new RestTemplate();
            String ResourceUrl = "http://10.60.71.16:8880/dvs/?billnumber=";
            ResponseEntity<NotifiRespDTO> response2= restTemplate.getForEntity(ResourceUrl + req.getBillAccount() + "&paymentdate=" + req.getPaymentDate() + "&token=" + response1.getBody().getAccess_token() , NotifiRespDTO.class);
            log.info("Succuss DVS update" + response2.getBody().getStatusId());
            // NotifiResp resp = (NotifiResp)response2.getBody(); // only for testing
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.OK);
        } else {
            return new ResponseEntity<NotifiRespDTO>(resp,  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public PaymentDTO createCreditCardPayment(PaymentDTO paymentDTO) {
        log.debug("Request to create cc Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        // payment = paymentRepository.save(payment);


        //Step 1: Generate Secure Hash
        // Use Yours, Please Store Your Secret Key in safe Place (eg. database)
        String SECRET_KEY = "OTJkZGM5YzRkZmI0NzU0OTNkNDU0MGNi";
        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        // Map <String,String> parameters = new TreeMap<String,String> ();
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        String transactionId = String.valueOf(System.currentTimeMillis());
        // fill required parameters
        parameters.add(new BasicNameValuePair("TransactionID", transactionId));
        parameters.add(new BasicNameValuePair("MerchantID", "010000085"));
        parameters.add(new BasicNameValuePair("Amount", "2000"));
        parameters.add(new BasicNameValuePair("CurrencyISOCode", "682"));
        parameters.add(new BasicNameValuePair("MessageID", "1"));
        parameters.add(new BasicNameValuePair("Quantity", "1"));
        parameters.add(new BasicNameValuePair("Channel", "0"));
        //fill some optional parameters
        parameters.add(new BasicNameValuePair("Language", "en"));
        parameters.add(new BasicNameValuePair("ThemeID", "1000000001"));
        // ToDO change this url
        parameters.add(new BasicNameValuePair("ResponseBackURL", "https://MerchantSite/RedirectPaymentResponsePage"));
        // if this url is configured for the merchant it's not required
        parameters.add(new BasicNameValuePair("Version", "1.0"));
        //Create an Ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(SECRET_KEY);
        for (NameValuePair pair : parameters) {
            orderedString.append(pair.getValue());
        }
        System.out.println("orderdString: " + orderedString);
        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        // Step 2: Prepare Payment Request and Send It to Redirect JSP Page (To Send a Post Request)
        /*request.setAttribute("TransactionID", transactionId);
        request.setAttribute("MerchantID", "ANBRedirectM");
        request.setAttribute("Amount", "2000");
        request.setAttribute("CurrencyISOCode", "840");
        request.setAttribute("MessageID", "1");
        request.setAttribute("Quantity", "1");
        request.setAttribute("Channel", "0");
        request.setAttribute("Language", "en");
        request.setAttribute("ThemeID", "1000000001");
        // if this url is configured for the merchant it's not required, else it is required
        request.setAttribute("ResponseBackURL", "http://MerchantSite/RedirectPaymentResponsePage");


        request.setAttribute("Version", "1.0");
        request.setAttribute("RedirectURL","http://SmartrouteURL/SmartRoutePaymentWEB/SRPayMsgHandler");
        // set secure hash in the request
        request.setAttribute("SecureHash", secureHash);
        request.getRequestDispatcher(response.encodeURL("SubmitRedirectPaymentRequest.jsp")).forward(request, response);*/

        // Step 3: submit the form
        parameters.add(new BasicNameValuePair("SecureHash", secureHash));
        HttpClient httpClient = new DefaultHttpClient();
        String url = "https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRPayMsgHandler";
        HttpPost httpPost = new HttpPost(url);
        String content = null;
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                content = EntityUtils.toString(respEntity);
                System.out.println(content);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PaymentDTO result = paymentMapper.toDto(payment);
        result.setResult(content);
        return result;
    }

}
