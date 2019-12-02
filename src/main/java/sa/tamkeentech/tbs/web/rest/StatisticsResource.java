package sa.tamkeentech.tbs.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.domain.enumeration.Month;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.PaymentService;
import sa.tamkeentech.tbs.service.RefundService;
import sa.tamkeentech.tbs.service.dto.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


@RestController
@RequestMapping("/api")
public class StatisticsResource {
    private final Logger log = LoggerFactory.getLogger(StatisticsResource.class);

    private final RefundService refundService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final InvoiceRepository invoiceRepository;
     private final RefundRepository refundRepository;
    public StatisticsResource(RefundService refundService, InvoiceService invoiceService, PaymentService paymentService , InvoiceRepository invoiceRepository, RefundRepository refundRepository) {
         this.refundService=refundService;
         this.invoiceService=invoiceService;
         this.paymentService=paymentService;
        this.invoiceRepository=invoiceRepository;
        this.refundRepository =refundRepository;
    }
    @GetMapping("/statistics")
    public StatisticsDTO getStatistics(){
        log.debug("Request to get Statistics");
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        Pageable pageable =Pageable.unpaged();
        long totalInvoice = invoiceService.findAll(pageable).getTotalElements();
        statisticsDTO.setTotalInvoice(totalInvoice);
        statisticsDTO.setTotalPaid( invoiceRepository.sumPaidInvoice());
        statisticsDTO.setAmountRefund(refundRepository.amountRefund());
        statisticsDTO.setIncome(invoiceRepository.sumIncome());
        return statisticsDTO;
    }

    @GetMapping("/chartStatistics")
    @ResponseBody
    public List<ChartStatisticsDTO> getChartStatistics(@RequestParam("Date")ZonedDateTime statisticsDate ,@RequestParam("Type") int typeSatistics){
        log.debug("Request to get Chart Statistics");
        if(statisticsDate == null){
            statisticsDate =ZonedDateTime.now();
        }
        Pageable pageable =Pageable.unpaged();
        List<ChartStatisticsDTO> chartStatisticsDTOList = new ArrayList<>();
        if(typeSatistics == TypeStatistics.MONTHLY.getValue()){
            List<Object[]> stats= invoiceService.getMonthlyStat(statisticsDate);
            YearMonth yearMonthObject = YearMonth.of(statisticsDate.getYear(), statisticsDate.getMonth());
            for(int i =1 ;i<= yearMonthObject.lengthOfMonth(); i++){
                ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                    .duration(statisticsDate.withDayOfMonth(i).withHour(00).withMinute(00).withSecond(00).withNano(00))
                    .type(TypeStatistics.MONTHLY)
                    .day(i)
                    .month(yearMonthObject.getMonth().getValue())
                    .totalInvoice(null)
                    .totalPaid(null)
                    .build();
                stats.stream().filter(stat -> {
                    return chartStatisticsDTO.getDuration().getDayOfMonth() == ((Timestamp) stat[0]).getDate();
                }).findFirst().ifPresent(stat -> {
                    chartStatisticsDTO.setTotalInvoice((BigInteger) stat[1]);
                    chartStatisticsDTO.setTotalPaid((BigInteger)stat[2]);
                });
                chartStatisticsDTOList.add(chartStatisticsDTO);
            }

        }else{
            List<Object[]> stats= invoiceService.getAnnualyStat(statisticsDate);
            for(int i =1 ;i<= 12; i++){
                ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                    .duration(statisticsDate.withMonth(i))
                    .month(i)
                    .type(TypeStatistics.ANNUAL)
                    .totalInvoice(null)
                    .totalPaid(null)
                    .build();
                stats.stream().filter(stat -> {
                    return chartStatisticsDTO.getDuration().getMonth().getValue() == ((Timestamp) stat[0]).getMonth()+1;
                }).findFirst().ifPresent(stat -> {
                    chartStatisticsDTO.setTotalInvoice((BigInteger) stat[1]);
                    chartStatisticsDTO.setTotalPaid((BigInteger)stat[2]);
                });

                chartStatisticsDTOList.add(chartStatisticsDTO);
            }
        }
        return chartStatisticsDTOList;


    }

}
