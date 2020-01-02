package sa.tamkeentech.tbs.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.StatisticsService;
import sa.tamkeentech.tbs.service.dto.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.*;


@RestController
@RequestMapping("/api")
public class StatisticsResource {
    private final Logger log = LoggerFactory.getLogger(StatisticsResource.class);

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
     private final RefundRepository refundRepository;
    private final ClientRepository clientRepository;
    private final StatisticsService statisticsService;

    public StatisticsResource(InvoiceService invoiceService, InvoiceRepository invoiceRepository, RefundRepository refundRepository, ClientRepository clientRepository, StatisticsService statisticsService) {
         this.invoiceService=invoiceService;
        this.invoiceRepository=invoiceRepository;
        this.refundRepository =refundRepository;
        this.clientRepository = clientRepository;
        this.statisticsService = statisticsService;
    }
    @PostMapping("/statistics")
    @ResponseBody

    public StatisticsDTO getStatistics(@RequestBody StatisticsRequestDTO chartStatisticsRequest ){
        log.debug("Request to get Statistics");

        long clientId =0;
        Optional<Client> client = clientRepository.findByClientId(chartStatisticsRequest.getClientId());
        if(client.isPresent()){
            clientId = client.get().getId();
        }
        List<Object[]> stats = statisticsService.prepareQuery( chartStatisticsRequest.getFromDate(),chartStatisticsRequest.getToDate(),clientId,chartStatisticsRequest.getType());
        BigDecimal income =  statisticsService.prepareIncomeQuery(chartStatisticsRequest.getFromDate(),chartStatisticsRequest.getToDate(),clientId);

        StatisticsDTO statisticsDTO = new StatisticsDTO();
        Pageable pageable =Pageable.unpaged();
        BigInteger totalInvoice = (BigInteger) stats.get(0)[0];
        BigInteger setTotalPaid = (BigInteger) stats.get(0)[1];
        BigDecimal amountRefund= statisticsService.prepareRefundQuery(chartStatisticsRequest.getFromDate(),chartStatisticsRequest.getToDate(),clientId);
        statisticsDTO.setTotalInvoice(totalInvoice);
        statisticsDTO.setTotalPaid(setTotalPaid);
        statisticsDTO.setAmountRefund(amountRefund != null? amountRefund: BigDecimal.ZERO);
        statisticsDTO.setIncome(income != null?income: BigDecimal.ZERO);
        return statisticsDTO;
    }

    @PostMapping("/chartStatistics")
    @ResponseBody
    public List<ChartStatisticsDTO> getChartStatistics(@RequestBody StatisticsRequestDTO chartStatisticsRequest ){
        log.debug("Request to get Chart Statistics");
        long clientId =0;
        if( chartStatisticsRequest.getFromDate()==null ){
            chartStatisticsRequest.setFromDate(ZonedDateTime.now());
        }
        Optional<Client> client = clientRepository.findByClientId(chartStatisticsRequest.getClientId());
        if(client.isPresent()){
            clientId = client.get().getId();
        }
        ZonedDateTime currentDate = ZonedDateTime.now();
        int currentDay ;
        List<ChartStatisticsDTO> chartStatisticsDTOList = new ArrayList<>();

        if(chartStatisticsRequest.getType() == TypeStatistics.MONTHLY){
            List<Object[]> stats = statisticsService.prepareQuery( chartStatisticsRequest.getFromDate(),chartStatisticsRequest.getToDate(),clientId,chartStatisticsRequest.getType());
            YearMonth yearMonthObject = YearMonth.of( chartStatisticsRequest.getFromDate().getYear(),  chartStatisticsRequest.getFromDate().getMonth());

            if(chartStatisticsRequest.getToDate() == null && chartStatisticsRequest.getFromDate().getMonth() == currentDate.getMonth()){
                currentDay = currentDate.getDayOfMonth();
            }else{
                currentDay = yearMonthObject.lengthOfMonth();
            }

            for(int i =1 ;i<= yearMonthObject.lengthOfMonth() && i <= currentDay ; i++){
                ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                    .duration(chartStatisticsRequest.getFromDate().withDayOfMonth(i).withHour(00).withMinute(00).withSecond(00).withNano(00))
                    .type(TypeStatistics.MONTHLY)
                    .day(i)
                    .month(yearMonthObject.getMonth().getValue())
                    .totalInvoice(BigInteger.ZERO)
                    .totalPaid(BigInteger.ZERO)
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
            List<Object[]> stats = statisticsService.prepareQuery( chartStatisticsRequest.getFromDate(),chartStatisticsRequest.getToDate(),clientId,chartStatisticsRequest.getType());
            for(int i =1 ;i<= 12; i++){
                ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                    .duration(chartStatisticsRequest.getFromDate().withMonth(i))
                    .month(i)
                    .type(TypeStatistics.ANNUAL)
                    .totalInvoice(BigInteger.ZERO)
                    .totalPaid(BigInteger.ZERO)
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
