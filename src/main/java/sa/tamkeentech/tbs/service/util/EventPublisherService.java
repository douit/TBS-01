package sa.tamkeentech.tbs.service.util;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.aop.event.TBSEventPub;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.*;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class EventPublisherService {

    private final SequenceUtil sequenceUtil;

    private final ClientService clientService;

    private final CustomerService customerService;

    private final ItemService itemService;

    private final InvoiceRepository invoiceRepository;

    private final PaymentMethodService paymentMethodService;

    private final PaymentRepository paymentRepository;

    @Autowired
    @Lazy
    PaymentService paymentService;

    @Autowired
    @Lazy
    PaymentMapper paymentMapper;


    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    public EventPublisherService(SequenceUtil sequenceUtil, ClientService clientService, CustomerService customerService, ItemService itemService, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, PaymentRepository paymentRepository) {
        this.sequenceUtil = sequenceUtil;
        this.clientService = clientService;
        this.customerService = customerService;
        this.itemService = itemService;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
        this.paymentRepository = paymentRepository;
    }

    @TBSEventPub(eventName = Constants.EventType.SADAD_INITIATE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TBSEventRespDTO<Integer> callSadadEvent(TBSEventReqDTO<String> eventReq) throws IOException, JSONException {
        Integer sadadResp = paymentService.callSadad(eventReq.getReq());
        TBSEventRespDTO<Integer> eventResp = TBSEventRespDTO.<Integer>builder().resp(sadadResp).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.SADAD_NOTIFICATION)
    public TBSEventRespDTO<NotifiRespDTO> sendPaymentNotification(TBSEventReqDTO<NotifiReqDTO> reqNotification, Invoice invoice) {
        NotifiRespDTO resp = paymentService.sendPaymentNotification(reqNotification.getReq(), invoice);
        TBSEventRespDTO<NotifiRespDTO> eventResp = TBSEventRespDTO.<NotifiRespDTO>builder().resp(resp).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.CREDIT_CARD_INITIATE)
    public TBSEventRespDTO<PaymentDTO> initiateCreditCardPaymentEvent(TBSEventReqDTO<PaymentDTO> reqNotification, Optional<Invoice> invoice) {
        PaymentDTO result = paymentService.initiateCreditCardPayment(reqNotification.getReq(), invoice);
        TBSEventRespDTO<PaymentDTO> eventResp = TBSEventRespDTO.<PaymentDTO>builder().resp(result).build();
        return eventResp;
    }
}
