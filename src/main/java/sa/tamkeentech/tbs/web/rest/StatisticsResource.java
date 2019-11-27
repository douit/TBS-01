package sa.tamkeentech.tbs.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.RefundService;
import sa.tamkeentech.tbs.service.dto.InvoiceDTO;
import sa.tamkeentech.tbs.service.dto.PaymentDTO;
import sa.tamkeentech.tbs.service.dto.RefundDTO;
import sa.tamkeentech.tbs.service.dto.StatisticsDTO;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api")
public class StatisticsResource {
    private final Logger log = LoggerFactory.getLogger(StatisticsResource.class);

    private final RefundService refundService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public StatisticsResource(RefundService refundService, InvoiceService invoiceService, PaymentService paymentService) {
         this.refundService=refundService;
         this.invoiceService=invoiceService;
         this.paymentService=paymentService;
    }
    @GetMapping("/statistics")
    public StatisticsDTO getStatistics(){
        log.debug("Request to get Statistics");
        StatisticsDTO statisticsDTO = null;
        Pageable pageable =Pageable.unpaged();
        long totalInvoice = invoiceService.findAll(pageable).getTotalElements();

        long numPaid =0;
        for(PaymentDTO pid : paymentService.findAll()){
            if(pid.getStatus() == PaymentStatus.PAID)
                numPaid++;
        }

        BigDecimal amountRefund = new BigDecimal(0);
        for(RefundDTO refund : refundService.findAll()){
            if(refund.getStatus() != PaymentStatus.PAID)
                amountRefund= amountRefund.add(refund.getAmount());
            }

        List<InvoiceDTO> invoices = invoiceService.findAll(pageable).getContent();
        BigDecimal income = new BigDecimal(0);
        for(InvoiceDTO invoice : invoices){
            income= income.add(invoice.getAmount());
        }
        statisticsDTO.setNumInvoice(totalInvoice);
        statisticsDTO.setNumPaid(numPaid);
        statisticsDTO.setAmountRefund(amountRefund);
        statisticsDTO.setIncome(income);

        return statisticsDTO;
    }

}
