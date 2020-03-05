package sa.tamkeentech.tbs.service.soapClient;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

public class SOAPConnector extends WebServiceGatewaySupport {

    public Object callWebService(String url, Object request){
        // return getWebServiceTemplate().marshalSendAndReceive(url, request);
        return getWebServiceTemplate().marshalSendAndReceive( url, request,
            webServiceMessage -> {
                ((SoapMessage)webServiceMessage).setSoapAction(
                    /*"http://10.4.7.60:7766/SADADWare/RefunUpload/Request/RefundUpload.asmx" */
                   /* "http://www.BrightWare.com.sa/SADADWare/RefundRq"*/
                    /*"http://www.BrightWare.com.sa/SADADWare"*/
                    /*"RefundRq"*/
                    /*"RefundUpload"*/
                    /*"http://tempuri.org/RefundUpload"*/
                    /*"http://tempuri.org/RefundUpload/RefundUpload_v1_SOAPBinding"*/
                    /*"http://www.BrightWare.com.sa/SADADWare/RefundUpload_v1"*/
                    /*"RefundUpload_v1"*/
                    /*"http://tempuri.org/RefundUpload/RefundUpload_v1"*/
                    /*"http://tempuri.org/RefundUpload_v1"*/
                    /*"createRefundRq"*/
                    "CreateRefund"
                );
                SoapBody soapBody = ((SoapMessage) webServiceMessage).getSoapBody();

                Transformer identityTransform = TransformerFactory.newInstance().newTransformer();
                DOMResult domResult = new DOMResult();
                identityTransform.transform(soapBody.getPayloadSource(), domResult);

                Node bodyContent = domResult.getNode(); // modify this

                Node req= bodyContent.getFirstChild();
                req.setPrefix("");
                NodeList nl = req.getChildNodes();


                for(int i=0;i<nl.getLength();i++){
                    Node n = nl.item(i);
                    Element el = ((Element) n);
                    el.removeAttribute("xmlns");
                }

                identityTransform.transform(new DOMSource(bodyContent), soapBody.getPayloadResult());

            } );
    }
}
