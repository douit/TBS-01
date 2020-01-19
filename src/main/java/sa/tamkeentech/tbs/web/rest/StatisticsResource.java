package sa.tamkeentech.tbs.web.rest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.domain.Role;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.repository.InvoiceRepository;
import sa.tamkeentech.tbs.repository.RefundRepository;
import sa.tamkeentech.tbs.repository.RoleRepository;
import sa.tamkeentech.tbs.service.InvoiceService;
import sa.tamkeentech.tbs.service.StatisticsService;
import sa.tamkeentech.tbs.service.UserService;
import sa.tamkeentech.tbs.service.dto.*;
import sa.tamkeentech.tbs.service.util.CommonUtils;

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
    private final UserService userService;

    public StatisticsResource(InvoiceService invoiceService, InvoiceRepository invoiceRepository, RefundRepository refundRepository, ClientRepository clientRepository, StatisticsService statisticsService, AccountResource accountResource, UserService userService) {
        this.invoiceService = invoiceService;
        this.invoiceRepository = invoiceRepository;
        this.refundRepository = refundRepository;
        this.clientRepository = clientRepository;
        this.statisticsService = statisticsService;
        this.userService = userService;

    }

    @PostMapping("/statistics")
    @ResponseBody

    public StatisticsDTO getStatistics(@RequestBody StatisticsRequestDTO chartStatisticsRequest) {
        log.debug("Request to get Statistics");
//        Optional<Client> client = clientRepository.findByClientId(chartStatisticsRequest.getClientId());
        StatisticsDTO statisticsDTO = new StatisticsDTO();

        List<Long> clientIds = userService.listClientIds(chartStatisticsRequest.getClientId());
        if (CollectionUtils.isNotEmpty(clientIds)) {
            List<Object[]> stats = statisticsService.prepareQuery(chartStatisticsRequest.getFromDate(), chartStatisticsRequest.getToDate(), clientIds, chartStatisticsRequest.getType());
            BigDecimal income = statisticsService.prepareIncomeQuery(chartStatisticsRequest.getFromDate(), chartStatisticsRequest.getToDate(), clientIds);
            BigInteger totalInvoice = (BigInteger) stats.get(0)[0];
            BigInteger setTotalPaid = (BigInteger) stats.get(0)[1];
            BigDecimal amountRefund = statisticsService.prepareRefundQuery(chartStatisticsRequest, clientIds);
            statisticsDTO.setTotalInvoice(totalInvoice);
            statisticsDTO.setTotalPaid(setTotalPaid);
            statisticsDTO.setAmountRefund(amountRefund != null ? amountRefund : BigDecimal.ZERO);
            statisticsDTO.setIncome(income != null ? income : BigDecimal.ZERO);
            return statisticsDTO;
        } else {
            return statisticsDTO.builder()
                .amountRefund(BigDecimal.ZERO)
                .totalInvoice(BigInteger.ZERO)
                .totalPaid(BigInteger.ZERO)
                .income(BigDecimal.ZERO).build();
        }
    }

    @PostMapping("/chartStatistics")
    @ResponseBody
    public List<ChartStatisticsDTO> getChartStatistics(@RequestBody StatisticsRequestDTO chartStatisticsRequest) {
        log.debug("Request to get Chart Statistics");
        List<Long> clientIds = userService.listClientIds(chartStatisticsRequest.getClientId());
        List<ChartStatisticsDTO> chartStatisticsDTOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(clientIds)) {
            if (chartStatisticsRequest.getFromDate() == null) {
                chartStatisticsRequest.setFromDate(ZonedDateTime.now());
            }

            // Get User timeZone day and month
            ZonedDateTime localFromDate = CommonUtils.getLocalDate(chartStatisticsRequest.getFromDate(), chartStatisticsRequest.getOffset());
            ZonedDateTime localToDate = CommonUtils.getLocalDate(chartStatisticsRequest.getToDate(), chartStatisticsRequest.getOffset());

            ZonedDateTime currentDate = ZonedDateTime.now();
            int currentDay;


            if (chartStatisticsRequest.getType() == TypeStatistics.MONTHLY) {
                List<Object[]> stats = statisticsService.prepareQuery(localFromDate, localToDate, clientIds, chartStatisticsRequest.getType());
                YearMonth yearMonthObject = YearMonth.of(localFromDate.getYear(), localFromDate.getMonth());

                if (localToDate == null && localFromDate.getMonth() == currentDate.getMonth()) {
                    currentDay = currentDate.getDayOfMonth();
                } else {
                    currentDay = yearMonthObject.lengthOfMonth();
                }

                for (int i = 1; i <= yearMonthObject.lengthOfMonth() && i <= currentDay; i++) {
                    ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                        .duration(localFromDate.withDayOfMonth(i).withHour(00).withMinute(00).withSecond(00).withNano(00))
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
                        chartStatisticsDTO.setTotalPaid((BigInteger) stat[2]);
                    });
                    chartStatisticsDTOList.add(chartStatisticsDTO);
                }

            } else {
                List<Object[]> stats = statisticsService.prepareQuery(localFromDate, localToDate, clientIds, chartStatisticsRequest.getType());
                for (int i = 1; i <= 12; i++) {
                    ChartStatisticsDTO chartStatisticsDTO = ChartStatisticsDTO.builder()
                        .duration(localFromDate.withMonth(i))
                        .month(i)
                        .type(TypeStatistics.ANNUAL)
                        .totalInvoice(BigInteger.ZERO)
                        .totalPaid(BigInteger.ZERO)
                        .build();
                    stats.stream().filter(stat -> {
                        return chartStatisticsDTO.getDuration().getMonth().getValue() == ((Timestamp) stat[0]).getMonth() + 1;
                    }).findFirst().ifPresent(stat -> {
                        chartStatisticsDTO.setTotalInvoice((BigInteger) stat[1]);
                        chartStatisticsDTO.setTotalPaid((BigInteger) stat[2]);
                    });

                    chartStatisticsDTOList.add(chartStatisticsDTO);
                }
            }
            return chartStatisticsDTOList;


        } else {
            return chartStatisticsDTOList;
        }
    }
}
