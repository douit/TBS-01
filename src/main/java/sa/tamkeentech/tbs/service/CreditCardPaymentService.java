package sa.tamkeentech.tbs.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.dto.PaymentStatusResponseDTO;
import sa.tamkeentech.tbs.web.rest.errors.PaymentGatewayException;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
public class CreditCardPaymentService {

    private final Logger log = LoggerFactory.getLogger(CreditCardPaymentService.class);

    @Inject
    @Lazy
    PaymentRepository paymentRepository;

    @Inject
    @Lazy
    InvoiceRepository invoiceRepository;

    @Inject
    PaymentService paymentService;

    @Value("${tbs.payment.sts-secret-key}")
    private String stsSecretKey;
    @Value("${tbs.payment.sts-merchant-id}")
    private String stsMerchantId;
    @Value("${tbs.payment.sts-post-form-url}")
    private String stsPostFormUrl;
    @Value("${tbs.payment.sts-response-back-url}")
    private String stsResponseBackUrl;
    @Value("${tbs.payment.sts-refund-and-inquiry}")
    private String stsCheckStatusUrl;

    public String initPayment(Model model, String transactionId) {
        log.info("Request to initiate Payment : {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new TbsRunTimeException("Payment not found");
        }
        Invoice invoice = payment.getInvoice();
        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new TbsRunTimeException("Invoice already paid");
        }

        payment.setStatus(PaymentStatus.CHECKOUT_PAGE);
        paymentRepository.save(payment);

        invoice.setPaymentStatus(PaymentStatus.CHECKOUT_PAGE);
        invoiceRepository.save(invoice);
        // Step 1: Generate Secure Hash
        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String,String> parameters = new TreeMap<>();
        // String transactionId = String.valueOf(System.currentTimeMillis());
        // fill required parameters
        parameters.put("TransactionID", transactionId);
        parameters.put("MerchantID", stsMerchantId);
        BigDecimal roundedAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
        parameters.put("Amount", roundedAmount.multiply(new BigDecimal("100")).toBigInteger().toString());
        parameters.put("CurrencyISOCode", "682");

        // desc to display in checkout page
        boolean includeDesc = true;
        if (invoice.getInvoiceItems() != null && invoice.getInvoiceItems().size() == 1) {
            String itemDetail = invoice.getInvoiceItems().get(0).getDetails();
            parameters.put("PaymentDescription", itemDetail);
        }

        parameters.put("ItemID", "181");
        parameters.put("MessageID", "1");
        parameters.put("Quantity", "1");
        parameters.put("Channel", "0");
        // fill some optional parameters
        parameters.put("Language", "Ar");
        parameters.put("ThemeID", "theme1");
        // if this url is configured for the merchant it's not required
        parameters.put("ResponseBackURL", stsResponseBackUrl);
        parameters.put("Version", "1.0");
        // Create an Ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(stsSecretKey);
        for (String treeMapKey : parameters.keySet()) {
            if (!treeMapKey.equalsIgnoreCase("PaymentDescription")) {
                orderedString.append(parameters.get(treeMapKey));
            } else {
                // ok only for english
                // orderedString.append(parameters.get(treeMapKey).replaceAll(" ", "+"));
                try {
                    String desc = URLEncoder.encode(parameters.get(treeMapKey), "UTF-8");
                    orderedString.append(desc);
                } catch (UnsupportedEncodingException e) {
                    includeDesc = false;
                    e.printStackTrace();
                }
            }
        }

        log.info("orderdString: " + orderedString);
        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        // Post form
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase("PaymentDescription") || includeDesc) {
                model.addAttribute(entry.getKey(), entry.getValue());
            }
        }
        model.addAttribute("SecureHash", secureHash);
        model.addAttribute("RedirectURL", stsPostFormUrl);
        return "submitPayment";
    }

    public void processPaymentNotification (HttpServletRequest request, HttpServletResponse response) throws IOException {
        Enumeration<String> parameterNames = request.getParameterNames();
        // store all response Parameters to generate Response Secure Hash
        // and get Parameters to use it later in your Code
        Map<String, String> responseParameters = new TreeMap<String, String>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            responseParameters.put(paramName, paramValue);
        }

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(getResponseOrderdString(responseParameters, stsSecretKey, true)).getBytes());
        log.info("-->generatedsecureHash is: " + generatedsecureHash);

        // get the received secure hash from result map
        String receivedSecurehash = responseParameters.get("Response.SecureHash");
        log.info("--> receivedSecurehash is: " + receivedSecurehash);

        String transactionId = responseParameters.get("Response.TransactionID");
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new PaymentGatewayException("STS notification, Payment not found");
        }
        Invoice invoice = payment.getInvoice();

        // Build PaymentStatusResponseDTO and call paymentService.updateCreditCardPaymentAndSendEvent
        PaymentStatusResponseDTO.PaymentStatusResponseDTOBuilder paymentStatusResp = PaymentStatusResponseDTO.builder()
            .code(responseParameters.get("Response.StatusCode"))
            .billNumber(invoice.getAccountId().toString())
            .transactionId(transactionId).description(responseParameters.get("Response.GatewayStatusDescription"))
            .cardNumber(responseParameters.get("Response.CardNumber")).cardExpiryDate(responseParameters.get("Response.CardExpiryDate"))
            .amount(responseParameters.get("Response.Amount"));
        if (!generatedsecureHash.equals(receivedSecurehash)) {
            // IF they are not equal then the response shall not be accepted
            log.error("--<<>>-- processPaymentNotification: Received Secure Hash does not Equal Generated Secure hash");
        } else {
            // Complete the Action get other parameters from result map and do your processes
            // Please refer to The Integration Manual to see the List of The Received Parameters
            log.info("Status is: {}", responseParameters.get("Response.StatusCode"));
            paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResp.build(), payment);
        }
        // ToDo replace by client redirectUrl
        String redirectUrl = invoice.getClient().getRedirectUrl() + "?transactionId=" + transactionId;
        log.info("------Redirect after payment to: {}", redirectUrl);
        response.addHeader("Location", redirectUrl);
    }



    public PaymentStatusResponseDTO checkOffilnePaymentStatus(String transactionID) {

        //Step 1: Generate Secure Hash

        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map <String,String> parameters = new TreeMap<String,String> ();


        // fill required parameters
        parameters.put("MessageID", "2"); parameters.put("OriginalTransactionID", transactionID);
        parameters.put("MerchantID", stsMerchantId); parameters.put("Version", "1.0");

        //Create an ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(stsSecretKey);
        for (String treeMapKey : parameters.keySet()) {
            orderedString.append(parameters.get(treeMapKey));
        }
        log.debug("orderdString "+orderedString);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString().getBytes()));

        StringBuffer requestQuery = new  StringBuffer ();
        requestQuery
            .append("OriginalTransactionID").append("=").append(transactionID)
            .append("&").append("MerchantID").append("=").append(stsMerchantId).append("&")
            .append("MessageID").append("=").append("2").append("&")
            .append("Version")
            .append("=").append("1.0").append("&")
            .append("SecureHash").append("=")
            .append(secureHash).append("&");


        //Send the request
        StringBuffer output =  new StringBuffer();
        try {
            URL url = new URL(stsCheckStatusUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");

            //write parameters
            writer.write(requestQuery.toString());
            writer.flush();

            // Get the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            throw new TbsRunTimeException("Check payment connexion issue with STS", e);
        }

        //Output the response
        System.out.println(output.toString());

        // this string is formatted as a "Query String" - name=value&name2=value2.......
        String outputString=output.toString();

        // To read the output string you might want to split it
        // on '&' to get pairs then on '=' to get name and value
        // and for a better and ease on verifying secure hash you should put them in a TreeMap String [] pairs=outputString.split("&");
        String [] pairs=outputString.split("&");
        Map<String,String> result=new TreeMap<String,String>();

        // now we have separated the pairs from each other {"name1=value1","name2=value2",....}
        for(String pair:pairs){
            // now we have separated the pair to {"name","value"}
            String[] nameValue=pair.split("=");
            String name=nameValue[0];
            String value=nameValue[1];
            result.put(name,value);
        }
        // Now that we have the map, order it to generate secure hash and compare it with the received one
        /*StringBuilder responseOrderdString = new StringBuilder(); responseOrderdString.append(stsSecretKey);

        for (String treeMapKey : result.keySet()) {
            if(result.get(treeMapKey)!= null && !result.get(treeMapKey).equals("null") &&  !treeMapKey.equals("Response.SecureHash")){
                responseOrderdString.append(result.get(treeMapKey));
                log.debug("---- responseOrderdString Adding: {} --> {}", treeMapKey, result.get(treeMapKey));
            }
        }
        log.debug("Response Orderd String is : " + responseOrderdString.toString());
        String formattedResponse = responseOrderdString.toString().replaceAll(" ", "+");

        log.debug("Formatted Orderd String is: " + formattedResponse);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(formattedResponse).getBytes());
        // String generatedsecureHash = new String(DigestUtils.sha256Hex(getResponseOrderdString(result)).getBytes());*/

        // Ahmed unify impl
        String generatedsecureHash = null;
        try {
            generatedsecureHash = new String(DigestUtils.sha256Hex(getResponseOrderdString(result, stsSecretKey, false)).getBytes());
        } catch (UnsupportedEncodingException e) {
            throw new TbsRunTimeException("--<<>>--Async: not able to generate Secure hash, {}", e);
        }

        // get the received secure hash from result map
        String receivedSecurehash = result.get("Response.SecureHash");
        log.debug("----> generatedsecureHash: {}", generatedsecureHash);
        log.debug("----> receivedSecurehash : {}", receivedSecurehash);
        if(!receivedSecurehash.equals(generatedsecureHash)){
            //IF they are not equal then the response shall not be accepted
            throw new TbsRunTimeException("--<<>>--Async: Received Secure Hash does not Equal generated Secure hash");
        }  else {
            // Complete the Action get other parameters from result map and do your processes // Please refer to The Integration Manual to See The List of The Received Parameters String status=result.get("Response.Status");
            PaymentStatusResponseDTO paymentStatusResponseDTO = PaymentStatusResponseDTO.builder()
                .code(result.get("Response.StatusCode"))
                .cardNumber(result.get("Response.CardNumber"))
                .transactionId(result.get("Response.TransactionID"))
                .cardHolderName(result.get("Response.CardHolderName"))
                //.billNumber(result.get())
                .cardExpiryDate(result.get("Response.CardExpiryDate"))
                .description(result.get("Response.StatusDescription"))
                .build();
            Payment payment = paymentRepository.findByTransactionId(result.get("Response.TransactionID"));

            paymentService.updateCreditCardPaymentAndSendEvent(paymentStatusResponseDTO, payment);
            return paymentStatusResponseDTO;
        }

    }

    public String getResponseOrderdString(Map<String, String> responseParameters, String key, boolean isArabicStatusDesc) throws UnsupportedEncodingException {
        // Now that we have the map, order it to generate secure hash and compare it with the received one
        StringBuilder responseOrderdString = new StringBuilder();

        responseOrderdString.append(key);
        for (String treeMapKey : responseParameters.keySet()) {

                log.info("--Param key--- {} : {}", treeMapKey, responseParameters.get(treeMapKey));
            if(!responseParameters.get(treeMapKey).equals("null")){
                switch (treeMapKey) {
                    case "Response.SecureHash":
                        log.info("ignoring Response.SecureHash");
                        break;
                    case "Response.GatewayStatusDescription":
                    // case "Response.GatewayName":
                    //case "Response.CardHolderName":
                        log.info("***case Response.GatewayStatusDescription");
                        String field = URLEncoder.encode(responseParameters.get(treeMapKey), "UTF-8");
                        log.info("-->After encoding: {}", field);
                        responseOrderdString.append(field);
                        break;
                    case "Response.StatusDescription":
                        log.info("***case Response.StatusDescription");
                        String statusDescription = URLEncoder.encode(responseParameters.get("Response.StatusDescription"), "UTF-8");
                        log.info("-->After encoding: {}", statusDescription);
                        // ToDO add language to client DB
                        if (isArabicStatusDesc) {
                            statusDescription = statusDescription.toUpperCase();
                        }
                        log.info("-->After Upper: {}", statusDescription);
                        responseOrderdString.append(statusDescription);
                        break;
                    default:
                        responseOrderdString.append(responseParameters.get(treeMapKey));
                        break;
                }
            }

        }
        log.info("Response Ordered String is: " + responseOrderdString.toString());
        return responseOrderdString.toString();
    }



    public static void main(String[] args) throws UnsupportedEncodingException {

        // String key = "OTJkZGM5YzRkZmI0NzU0OTNkNDU0MGNi"; // staging
        String key = "NzllYTk4MjU0ZTQ5M2JjZTJmMzg4NGRi"; // prod

        // online check: processPaymentNotification
        /*Map<String, String> mapOnline = new TreeMap<>();
        mapOnline.put("Response.Amount", "100");
        mapOnline.put("Response.ApprovalCode", "");
        mapOnline.put("Response.CardExpiryDate", "2201");
        mapOnline.put("Response.CardHolderName", "Ahmed Bouzaien");
        mapOnline.put("Response.CardNumber", "484783******5202");
        mapOnline.put("Response.CurrencyISOCode", "682");
        mapOnline.put("Response.GatewayName", "ANB PG");
        mapOnline.put("Response.GatewayStatusCode", "0000");
        mapOnline.put("Response.GatewayStatusDescription", "Payment processed successfully.");
        mapOnline.put("Response.MerchantID", "010000085");
        mapOnline.put("Response.MessageID", "1");
        mapOnline.put("Response.RRN", "202003160029354540000000000");
        mapOnline.put("Response.SecureHash", "44f02cb84acc1296048bfb4f31ecc2bc4aea6ebd57c37a9e660f8443ab97f469");
        mapOnline.put("Response.StatusCode", "00000");
        mapOnline.put("Response.StatusDescription", "تمت الحركة بنجاح");
        mapOnline.put("Response.TransactionID", "7000000235232752");

        CreditCardPaymentService paymentserv = new CreditCardPaymentService();
        String generatedsecureHash = new String(DigestUtils.sha256Hex(paymentserv.getResponseOrderdString(mapOnline, key)).getBytes());
        System.out.println("-->generatedsecureHash is: " + generatedsecureHash);
        System.out.println("-->receivedSecurehash is: " + "44f02cb84acc1296048bfb4f31ecc2bc4aea6ebd57c37a9e660f8443ab97f469");*/



        // offline check
        // simulte resp
        Map<String, String> map = new TreeMap<>();
        //map.put("key", key);
        map.put("Response.Amount", "100");
        //map.put("Response.ApprovalCode", "");
        map.put("Response.CardExpiryDate", "2201");
        map.put("Response.CardHolderName", "Ahmed Bouzaien");
        map.put("Response.CardNumber", "484783******5202");
        map.put("Response.CurrencyISOCode", "682");
        map.put("Response.GatewayName", "ANB PG");
        map.put("Response.GatewayStatusCode", "0000");
        map.put("Response.GatewayStatusDescription", "Payment processed successfully.");
        map.put("Response.MerchantID", "010000085");
        map.put("Response.MessageID", "2");
        map.put("Response.MessageStatus", "00000");
        map.put("Response.RRN", "202003160029354540000000000");
        map.put("Response.ReversalStatus", "1");
        map.put("Response.StatusCode", "00000");
        map.put("Response.StatusDescription", "Transaction was processed successfully");
        map.put("Response.TransactionID", "7000000235232752");

        // Now that we have the map, order it to generate secure hash and compare it with the received one
        /*StringBuilder responseOrderdString = new StringBuilder(); responseOrderdString.append(key);

        for (String treeMapKey : map.keySet()) {
            if(map.get(treeMapKey)!= null && !map.get(treeMapKey).equals("null") &&  !treeMapKey.equals("Response.SecureHash")){
                String field;
                if (treeMapKey.equals("Response.GatewayStatusDescription") || treeMapKey.equals("Response.StatusDescription")) {
                    field = map.get(treeMapKey).replaceAll(" ", "+");
                } else {
                    field = map.get(treeMapKey);
                }
                responseOrderdString.append(field);
                System.out.println("---- responseOrderdString Adding: " + treeMapKey +  " --> " + field);
            }
        }
        System.out.println("Response Orderd String is : " + responseOrderdString.toString());
        // String formattedResponse = responseOrderdString.toString().replaceAll(" ", "+");

        // System.out.println("Formatted Orderd String is: " + formattedResponse);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(responseOrderdString.toString()).getBytes());*/

        // Ahmed unify impl
        String generatedsecureHash = null;
        try {
            CreditCardPaymentService paymentserv = new CreditCardPaymentService();
            generatedsecureHash = new String(DigestUtils.sha256Hex(paymentserv.getResponseOrderdString(map, key, false)).getBytes());
        } catch (UnsupportedEncodingException e) {
            throw new TbsRunTimeException("--<<>>--Async: not able to generate Secure hash, {}", e);
        }

        System.out.println("generatedsecureHash: " + generatedsecureHash);


        System.out.println("-----------------------");
        System.out.println("Test ZonedDateTime: " + ZonedDateTime.now());

        File directory = new File("C:\\sadad_share\\");
        File[] files = directory.listFiles((File dir, String name) -> {
            // String lowercaseName = name.toLowerCase();
            if ((name.startsWith("BLRCRQ-"))
                && name.endsWith(".xml")) {
                    return true;
            }
            return false;
        });
        if (files == null) {
            return;
        }
        Set banks = new HashSet<String>();
        int total = 0;
        for (File file : files) {
            Date lastMod = new Date(file.lastModified());
            // ZonedDateTime lastModZoneDate = ZonedDateTime.ofInstant(lastMod.toInstant(), ZoneId.of("Asia/Riyadh"));
            ZonedDateTime lastModZoneDate = ZonedDateTime.ofInstant(lastMod.toInstant(), ZoneId.systemDefault());
            System.out.println("----Processing Sadad File: " + file.getName() + ", Date: " + lastModZoneDate);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                NodeList nodeList = document.getDocumentElement().getElementsByTagName("PmtBankRec");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node refundRecordNode = nodeList.item(i);

                    // RefundStatus node
                    Node refundStatusNode = ((Element) refundRecordNode).getElementsByTagName("BankId").item(0);
                    // Get the value of RefundStatusCode.
                    String status = refundStatusNode.getChildNodes().item(0).getNodeValue();
                    if (!banks.contains(status)) {
                        total ++;
                        banks.add(status);
                    }
                }

            } catch (Exception e) {

            }
            System.out.println("----total: " + total);
            System.out.println("----banks: " + banks);
        }
        System.out.println("----All: " + total);
        System.out.println("----All banks: " + banks);

                    }

}
