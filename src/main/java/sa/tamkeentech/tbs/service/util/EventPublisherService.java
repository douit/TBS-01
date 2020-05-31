package sa.tamkeentech.tbs.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.aop.event.TBSEventPub;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.Invoice;
import sa.tamkeentech.tbs.domain.Payment;
import sa.tamkeentech.tbs.domain.Refund;
import sa.tamkeentech.tbs.domain.enumeration.PaymentProvider;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.schemas.refund.RefundRqType;
import sa.tamkeentech.tbs.service.*;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.mapper.PaymentMapper;

import javax.inject.Inject;
import java.io.IOException;
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

    @Autowired
    @Lazy
    InvoiceService invoiceService;

    @Autowired
    @Lazy
    RefundService refundService;


    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${tbs.payment.sadad-url}")
    private String sadadUrl;

    @Autowired
    private Environment environment;

    @Lazy
    @Inject
    private STSPaymentService stsPaymentService;

    @Lazy
    @Inject
    private PayFortPaymentService payFortPaymentService;

    public EventPublisherService(SequenceUtil sequenceUtil, ClientService clientService, CustomerService customerService, ItemService itemService, InvoiceRepository invoiceRepository, PaymentMethodService paymentMethodService, PaymentRepository paymentRepository) {
        this.sequenceUtil = sequenceUtil;
        this.clientService = clientService;
        this.customerService = customerService;
        this.itemService = itemService;
        this.invoiceRepository = invoiceRepository;
        this.paymentMethodService = paymentMethodService;
        this.paymentRepository = paymentRepository;
    }

    @TBSEventPub(eventName = Constants.EventType.INVOICE_CREATE)
    public TBSEventRespDTO<InvoiceResponseDTO> saveOneItemInvoiceEvent(TBSEventReqDTO<OneItemInvoiceDTO> eventReq) {
        InvoiceResponseDTO resp = invoiceService.saveOneItemInvoice(eventReq.getReq(), eventReq.getLanguage());
        TBSEventRespDTO<InvoiceResponseDTO> eventResp = TBSEventRespDTO.<InvoiceResponseDTO>builder().referenceId(resp.getBillNumber()).resp(resp).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.INVOICE_CREATE)
    public TBSEventRespDTO<InvoiceResponseDTO> saveInvoiceEvent(TBSEventReqDTO<InvoiceDTO> eventReq) {
        InvoiceResponseDTO resp = invoiceService.saveInvoice(eventReq.getReq(), eventReq.getLanguage());
        TBSEventRespDTO<InvoiceResponseDTO> eventResp = TBSEventRespDTO.<InvoiceResponseDTO>builder().referenceId(resp.getBillNumber()).resp(resp).build();
        return eventResp;
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
        NotifiRespDTO resp = paymentService.sendSadadPaymentNotification(reqNotification.getReq(), invoice);
        TBSEventRespDTO<NotifiRespDTO> eventResp = TBSEventRespDTO.<NotifiRespDTO>builder().resp(resp).build();
        return eventResp;
    }

    /*@TBSEventPub(eventName = Constants.EventType.CREDIT_CARD_PAYMENT_REQUEST)
    public TBSEventRespDTO<PaymentDTO> initiateCreditCardPaymentEvent(TBSEventReqDTO<PaymentDTO> reqNotification, Optional<Invoice> invoice) {
        PaymentDTO result = paymentService.initiateCreditCardPayment(reqNotification.getReq(), invoice);
        TBSEventRespDTO<PaymentDTO> eventResp = TBSEventRespDTO.<PaymentDTO>builder().resp(result).build();
        return eventResp;
    }*/

    /*@TBSEventPub(eventName = Constants.EventType.CREDIT_CARD_INITIATE)
    public TBSEventRespDTO<PaymentResponseDTO> callCreditCardInitiateEvent(TBSEventReqDTO<String> event) throws IOException {
        PaymentResponseDTO result = paymentService.callCreditCard(event.getReq());
        TBSEventRespDTO<PaymentResponseDTO> eventResp = TBSEventRespDTO.<PaymentResponseDTO>builder().resp(result).build();
        return eventResp;
    }*/

    @TBSEventPub(eventName = Constants.EventType.CREDIT_CARD_NOTIFICATION)
    public TBSEventRespDTO<PaymentDTO> creditCardNotificationEvent(TBSEventReqDTO<PaymentStatusResponseDTO> reqNotification, Payment payment, Invoice invoice) {
        PaymentDTO result = paymentService.updateCreditCardPayment(reqNotification.getReq(), payment, invoice);
        TBSEventRespDTO<PaymentDTO> eventResp = TBSEventRespDTO.<PaymentDTO>builder().resp(result).build();
        return eventResp;
    }

    // both CC and Sadad
    @TBSEventPub(eventName = Constants.EventType.INVOICE_REFUND_REQUEST)
    public TBSEventRespDTO<RefundDTO> createNewRefund(TBSEventReqDTO<RefundDTO> eventReq, Invoice invoice, Optional<Payment> payment) throws IOException {
        RefundDTO result = refundService.createNewRefund(eventReq.getReq(), invoice, payment);
        TBSEventRespDTO<RefundDTO> eventResp = TBSEventRespDTO.<RefundDTO>builder().resp(result).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.CREDIT_CARD_REFUND_REQUEST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TBSEventRespDTO<RefundStatusCCResponseDTO> callRefundByCreditCardEvent(TBSEventReqDTO<Refund> eventReq, Invoice invoice, Optional<Payment> payment) throws IOException {
        RefundStatusCCResponseDTO refundStatusCCResponseDTO;
        if(payment.get().getPaymentProvider().equals(PaymentProvider.STS)){
            refundStatusCCResponseDTO = stsPaymentService.proceedRefundOperation(eventReq.getReq(), invoice, payment);
        }else {
            refundStatusCCResponseDTO =  payFortPaymentService.proceedRefundOperation(eventReq.getReq(), invoice, payment);
        }
        TBSEventRespDTO<RefundStatusCCResponseDTO> eventResp = TBSEventRespDTO.<RefundStatusCCResponseDTO>builder().resp(refundStatusCCResponseDTO).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.SADAD_REFUND_REQUEST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TBSEventRespDTO<RefundStatusSadadResponseDTO> callSadadRefundEvent(TBSEventReqDTO<String> eventReq) throws IOException {
        RefundStatusSadadResponseDTO sadadResp;
        if (CommonUtils.isProfile(environment, "prod") || CommonUtils.isProfile(environment, "ahmed")) {
            sadadResp = refundService.callRefundBySdad(eventReq.getReq());
        } else {
            log.debug("----Sadad refund request : {}", eventReq.getReq());
            log.info("----Sadad refund response status code : ***** Mocking *** eventReq.getReq()");
            sadadResp = RefundStatusSadadResponseDTO.builder().refundResult(RefundStatusSadadResponseDTO.RefundResult.builder()
                .status(RefundStatusSadadResponseDTO.Status.builder().code("0").description("Fake resp").build()).build()).build();
        }
        TBSEventRespDTO<RefundStatusSadadResponseDTO> eventResp = TBSEventRespDTO.<RefundStatusSadadResponseDTO>builder().resp(sadadResp).build();
        return eventResp;
    }

    @TBSEventPub(eventName = Constants.EventType.SADAD_REFUND_NOTIFICATION)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TBSEventRespDTO<Boolean> updateSadadRefund(TBSEventReqDTO<RefundDTO> reqNotification) {
        Boolean sadadSadadRefundResp = refundService.updateSadadRefund(reqNotification.getReq().getId());
        TBSEventRespDTO<Boolean> eventResp = TBSEventRespDTO.<Boolean>builder().resp(sadadSadadRefundResp).build();
        return eventResp;
    }
}
