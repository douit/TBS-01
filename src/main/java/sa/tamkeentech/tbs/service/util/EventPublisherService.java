package sa.tamkeentech.tbs.service.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.aop.event.TBSEventPub;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.service.ClientService;
import sa.tamkeentech.tbs.service.CustomerService;
import sa.tamkeentech.tbs.service.ItemService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.dto.TBSEventReqDTO;
import sa.tamkeentech.tbs.service.dto.TBSEventRespDTO;

import java.io.IOException;

@Service
public class EventPublisherService {

    private final SequenceUtil sequenceUtil;

    private final ClientService clientService;

    private final CustomerService customerService;

    private final ItemService itemService;

    private final InvoiceRepository invoiceRepository;

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    public EventPublisherService(SequenceUtil sequenceUtil, ClientService clientService, CustomerService customerService, ItemService itemService, InvoiceRepository invoiceRepository) {
        this.sequenceUtil = sequenceUtil;
        this.clientService = clientService;
        this.customerService = customerService;
        this.itemService = itemService;
        this.invoiceRepository = invoiceRepository;
    }

    @TBSEventPub(eventName = Constants.EventType.SADAD_INITIATE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TBSEventRespDTO<Integer> callSadad(TBSEventReqDTO<String> eventReq) throws IOException, JSONException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(sadadUrl);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(eventReq.getReq()));
        HttpResponse response;
        response = client.execute(post);

        log.debug("----Sadad request : {}", eventReq);
        log.info("----Sadad response status code : {}", response.getStatusLine().getStatusCode());
        if (response.getEntity() != null) {
            log.debug("----Sadad response content : {}", response.getEntity().getContent());
            log.debug("----Sadad response entity : {}", response.getEntity().toString());
        }

        TBSEventRespDTO<Integer> eventResp = new TBSEventRespDTO();
        eventResp.setResp(response.getStatusLine().getStatusCode());
        return eventResp;
    }
}
