package sa.tamkeentech.tbs.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

@Service
public class CreditCardPaymentService {

    private final Logger log = LoggerFactory.getLogger(CreditCardPaymentService.class);

    // Use Yours, Please Store Your Secret Key in safe Place (eg. database)
    public static final String SECRET_KEY = "OTJkZGM5YzRkZmI0NzU0OTNkNDU0MGNi";

    public String initPayment(Model model) {
        //Step 1: Generate Secure Hash
        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map<String, String> parameters = new TreeMap<String, String>();

        String transactionId = String.valueOf(System.currentTimeMillis());
        // fill required parameters
        parameters.put("TransactionID", transactionId);
        parameters.put("MerchantID", "010000085");
        parameters.put("Amount", "2000");
        parameters.put("CurrencyISOCode", "682");
        parameters.put("ItemID", "181");
        parameters.put("MessageID", "1");
        parameters.put("Quantity", "1");
        parameters.put("Channel", "0");

        //fill some optional parameters
        parameters.put("Language", "Ar");
        parameters.put("ThemeID", "theme1");
        // if this url is configured for the merchant it's not required
        parameters.put("ResponseBackURL", "https://platform.tamkeentech.sa/tamkeen-billing-system/STSResponse");
        parameters.put("Version", "1.0");

        //Create an Ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder();
        orderedString.append(SECRET_KEY);
        for (String treeMapKey : parameters.keySet()) {
            orderedString.append(parameters.get(treeMapKey));
        }

        System.out.println("orderdString: " + orderedString);
        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        // Post form
        String url = "https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRPayMsgHandler";
        /*HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        params.add(new BasicNameValuePair("SecureHash", secureHash));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                String content = EntityUtils.toString(respEntity);
                System.out.println(content);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // --> replace form by thymeleaf

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            model.addAttribute(entry.getKey(), entry.getValue());
        }
        model.addAttribute("SecureHash", secureHash);
        model.addAttribute("RedirectURL", url);


        return "submitPayment";
    }


    public CCPaymentStatusResponseDTO checkPaymentStatus(String transactionID) throws IOException {

        //Step 1: Generate Secure Hash

        // put the parameters in a TreeMap to have the parameters to have them sorted alphabetically.
        Map <String,String> parameters = new TreeMap<String,String> ();
        String transactionId=String.valueOf(System.currentTimeMillis());

        // fill required parameters
        parameters.put("MessageID", "2"); parameters.put("OriginalTransactionID", "1440954863817");
        parameters.put("MerchantID", "010000085"); parameters.put("Version", "1.0");

        //Create an ordered String of The Parameters Map with Secret Key
        StringBuilder orderedString = new StringBuilder(); orderedString.append(SECRET_KEY);
        for (String treeMapKey : parameters.keySet()) {
            orderedString.append(parameters.get(treeMapKey));
        }
        System.out.println("orderdString "+orderedString);

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String secureHash = new String(DigestUtils.sha256Hex(orderedString.toString()).getBytes());

        StringBuffer requestQuery = new  StringBuffer ();
        requestQuery
            .append("OriginalTransactionID").append("=").append(transactionID)
            .append("&").append("MerchantID").append("=").append("010000085").append("&")
            .append("MessageID").append("=").append("2").append("&") .append("Version")
            .append("=").append("1.0").append("&") .append("SecureHash").append("=")
            .append(secureHash).append("&");


        //Send the request
        URL url = new URL("https://srstaging.stspayone.com/SmartRoutePaymentWeb/SRMsgHandler");
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
            String name=nameValue[0];//first element is the name
            String value=nameValue[1];//second element is the value
            // put the pair in the result map
            result.put(name,value);
        }

        // Now that we have the map, order it to generate secure hash and compare it with the received one
        StringBuilder responseOrderdString = new StringBuilder(); responseOrderdString.append(SECRET_KEY);

        for (String treeMapKey : result.keySet()) { responseOrderdString.append(result.get(treeMapKey));
        }
        System.out.println("Response Orderd String is " + responseOrderdString.toString());

        // Generate SecureHash with SHA256
        // Using DigestUtils from appache.commons.codes.jar Library
        String generatedsecureHash = new String(DigestUtils.sha256Hex(responseOrderdString.toString()).getBytes());

        // get the received secure hash from result map
        String receivedSecurehash=result.get("Response.SecureHash");
        if(!receivedSecurehash.equals(generatedsecureHash)){
            //IF they are not equal then the response shall not be accepted
            throw new TbsRunTimeException("Received Secure Hash does not Equal generated Secure hash");
        }
        else{
            // Complete the Action get other parameters from result map and do your processes // Please refer to The Integration Manual to See The List of The Received Parameters String status=result.get("Response.Status");
            CCPaymentStatusResponseDTO paymentStatusResponseDTO = CCPaymentStatusResponseDTO.builder()
                .code(result.get("Response.Status"))
                .cardNumber(result.get("Response.CardNumber"))
                .transactionId(result.get("Response.TransactionID"))
                .build();

            if(){

            }
            return paymentStatusResponseDTO;
        }




    }

    public String getResponseOrderdString(Map<String, String> responseParameters) throws UnsupportedEncodingException {
        // Now that we have the map, order it to generate secure hash and compare it with the received one
        StringBuilder responseOrderdString = new StringBuilder();

        responseOrderdString.append(CreditCardPaymentService.SECRET_KEY);
        for (String treeMapKey : responseParameters.keySet()) {
            log.info("--Param key--- {} : {}", treeMapKey, responseParameters.get(treeMapKey));
            switch (treeMapKey) {
                case "Response.SecureHash":
                    log.info("ignoring Response.SecureHash");
                    break;
                case "Response.GatewayStatusDescription":
                    log.info("***case Response.GatewayStatusDescription");
                    String gatewayStatusDescription = URLEncoder.encode(responseParameters.get("Response.GatewayStatusDescription"), "UTF-8");
                    log.info("-->After encoding: {}", gatewayStatusDescription);
                    responseOrderdString.append(gatewayStatusDescription);
                    break;
                case "Response.StatusDescription":
                    log.info("***case Response.StatusDescription");
                    String statusDescription = URLEncoder.encode(responseParameters.get("Response.StatusDescription"), "UTF-8");
                    log.info("-->After encoding: {}", statusDescription);
                    // ToDO add language to client DB
                    if (true) {
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
        log.info("Response Ordered String is: " + responseOrderdString.toString());
        return responseOrderdString.toString();
    }

    // Testing
    // Response Ordered String is
    private static String responseOrderdString = "OTJkZGM5YzRkZmI0NzU0OTNkNDU0MGNi20002201Test401200******1112682ANBPaymentGateway0000Payment processed successfully.0100000851202002240842129840000000000ade8c0a20b1742c3a49e8583adb89de5e06ae4aac551822b40b9e5131a3d3c5200000تمت الحركة بنجاح1582522876285";


    public static void main(String[] args) throws UnsupportedEncodingException {

        /*var generatedsecureHash = "";
        bytes = Encoding.UTF8.GetBytes(responseOrderdstring.ToString());
        sha256 = SHA256.Create();
        hash = sha256.ComputeHash(bytes);
        foreach (byte x in hash)
        {
            generatedsecureHash += string.Format("{0:x2}", x);
        }*/

        // simulte resp
        Map<String, String> map = new TreeMap<>();
        map.put("Response.Amount", "2000");
        map.put("Response.ApprovalCode", "");
        map.put("Response.CardExpiryDate", "2201");
        map.put("Response.CardHolderName", "test");
        map.put("Response.CardNumber", "401200******1112");
        map.put("Response.CurrencyISOCode", "682");
        map.put("Response.GatewayName", "ANBPaymentGateway");
        map.put("Response.GatewayStatusCode", "0000");
        map.put("Response.GatewayStatusDescription", "Payment processed successfully.");
        map.put("Response.MerchantID", "010000085");
        map.put("Response.MessageID", "1");
        map.put("Response.RRN", "202002241052396110000000000");
        map.put("Response.SecureHash", "b5bbac4706119610382ba5b5e5e630a3345898da31b41527bbbd79b17d238315");
        map.put("Response.StatusCode", "00000");
        map.put("Response.StatusDescription", "تمت الحركة بنجاح");
        map.put("Response.TransactionID", "1582530735527");

        CreditCardPaymentService paymentserv = new CreditCardPaymentService();
        String resp = paymentserv.getResponseOrderdString(map);


        String generatedsecureHash = new String(DigestUtils.sha256Hex(resp.toString()).getBytes());
        //byte[] bytes = DigestUtils.sha256(responseOrderdString.toString().getBytes(StandardCharsets.UTF_8));
        //String generatedsecureHash = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("generatedsecureHash: " + generatedsecureHash);
        // must be ade8c0a20b1742c3a49e8583adb89de5e06ae4aac551822b40b9e5131a3d3c52


        System.out.println(java.net.URLEncoder.encode("Payment processed successfully.", "UTF-8"));
        System.out.println(URLEncoder.encode("Payment processed successfully.", "UTF-8"));

    }

}
