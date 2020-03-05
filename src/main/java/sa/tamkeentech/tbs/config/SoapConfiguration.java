package sa.tamkeentech.tbs.config;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import sa.tamkeentech.tbs.service.soapClient.SOAPConnector;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SoapConfiguration {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // this is the package name specified in the <generatePackage> specified in
        // pom.xml
        marshaller.setContextPath("sa.tamkeentech.tbs.schemas.refund");

        // fix name space issue
        /*Map<String, Object> prop = new HashMap<>();
        prop.put(Marshaller.JAXB_FORMATTED_OUTPUT,  Boolean.TRUE);
        prop.put("com.sun.xml.bind.namespacePrefixMapper", new MyNamespaceMapper());
        marshaller.setMarshallerProperties(prop);*/
        return marshaller;
    }

    @Bean
    public SOAPConnector soapConnector(Jaxb2Marshaller marshaller) {
        SOAPConnector client = new SOAPConnector();
        client.setDefaultUri("http://10.4.7.60:7766/SADADWare/RefunUpload/Request/RefundUpload.asmx");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}

class MyNamespaceMapper extends NamespacePrefixMapper {

    private static final String RefundRq_PREFIX = ""; // DEFAULT NAMESPACE
    private static final String RefundRq_URI = "http://www.BrightWare.com.sa/SADADWare";

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        /*if(RefundRq_URI.equals(namespaceUri)) {
            return RefundRq_PREFIX;
        }
        return suggestion;*/
        return RefundRq_PREFIX;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { RefundRq_URI };
    }

}
