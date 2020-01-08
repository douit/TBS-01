package sa.tamkeentech.tbs.service;

import liquibase.database.core.FirebirdDatabase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;
import sa.tamkeentech.tbs.service.dto.StatisticsRequestDTO;
import sa.tamkeentech.tbs.service.util.CommonUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;


import static sa.tamkeentech.tbs.domain.enumeration.TypeStatistics.*;

/**
 * Service Implementation for managing {@link PaymentMethod}.
 */
@Service
@Transactional
public class StatisticsService {

    private final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    @PersistenceContext
    private EntityManager entityManager;


    public StatisticsService() {

    }

    public List<Object[]> getInvoicesUsingWhereClause(EntityManager em, String whereClause, TypeStatistics type, List<Long> clientIds) {
        Query query = null;

        switch (type) {
            case GENERAL:
                query = em.createNativeQuery(
                    "SELECT count(*) As totalInvoice , \n " +
                        "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                        "      FROM invoice " +
                        ((StringUtils.isNotEmpty(whereClause)) ? " WHERE " + whereClause : ""));
                break;
            case ANNUAL:
                query = em.createNativeQuery(
                    "SELECT date_trunc('month', created_date) As Month , count(*) As totalInvoice , \n " +
                        "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                        "      FROM invoice " +
                        ((StringUtils.isNotEmpty(whereClause)) ? " WHERE " + whereClause : "") +
                        "      group by Month ORDER BY Month");
                break;
            case MONTHLY:
                query = em.createNativeQuery(
                    "SELECT date_trunc('day', created_date) As Day , count(*) As totalInvoice , \n " +
                        "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                        "      FROM invoice " +
                        ((StringUtils.isNotEmpty(whereClause)) ? " WHERE " + whereClause : "") +
                        "      group by Day ORDER BY Day");
                break;
            default:
                break;
        }
        if (CollectionUtils.isNotEmpty(clientIds)) {
            query.setParameter("clientIds", clientIds);
        }
        return query.getResultList();

    }

    public List<Object[]> prepareQuery(ZonedDateTime firstDate, ZonedDateTime lastDate, List<Long> clientIds, TypeStatistics type) {
        ZonedDateTime first = null;
        ZonedDateTime last = null;
        YearMonth yearMonthObject = null;

        String whereClause = null;
        switch (type) {
            case GENERAL:
                    if (firstDate == null && lastDate == null) {
                        whereClause = "";
                    } else if (firstDate != null && lastDate == null) {
                        whereClause = "created_date >= '" + firstDate.toLocalDateTime() + "'";
                    } else if (firstDate != null && lastDate != null) {
                        whereClause = "created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date <= '" + lastDate.toLocalDateTime() + "'";
                    } else if (firstDate == null && lastDate != null) {
                        whereClause = "created_date <= '" + lastDate.toLocalDateTime() + "'";
                    }
                if (CollectionUtils.isNotEmpty(clientIds)) {
                    whereClause = (StringUtils.isEmpty(whereClause)) ? " client_id IN :clientIds " :
                        whereClause + " client_id IN :clientIds ";
                }
                    break;
            case ANNUAL:
                first = CommonUtils.addSecondsToDate(-firstDate.getOffset().getTotalSeconds(),
                    firstDate.withMonth(1).withDayOfMonth(1).withHour(00).withMinute(00).withSecond(00).withNano(00));
                yearMonthObject = YearMonth.of(firstDate.getYear(), firstDate.getMonth());
                last = CommonUtils.addSecondsToDate(-firstDate.getOffset().getTotalSeconds(),
                    firstDate.withMonth(12).withDayOfMonth(yearMonthObject.lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999));
                if (first != null && last != null) {
                    whereClause = "created_date >= '" + first.toLocalDateTime() + "' AND created_date <= '" + last.toLocalDateTime() + "'";

                }
                if (CollectionUtils.isNotEmpty(clientIds)) {
                    whereClause = whereClause + " AND client_id IN :clientIds ";
                }
                break;
            case MONTHLY:
                first = CommonUtils.addSecondsToDate(-firstDate.getOffset().getTotalSeconds(),
                    firstDate.withDayOfMonth(1).withHour(00).withMinute(00).withSecond(00).withNano(00));
                yearMonthObject = YearMonth.of(firstDate.getYear(), firstDate.getMonth());
                last = CommonUtils.addSecondsToDate(-firstDate.getOffset().getTotalSeconds(),
                    firstDate.withDayOfMonth(yearMonthObject.lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999));


                if (first != null && last != null) {
                    whereClause = "created_date >= '" + first.toLocalDateTime() + "' AND created_date <= '" + last.toLocalDateTime() + "'";

                }
                if (CollectionUtils.isNotEmpty(clientIds)) {
                    whereClause = whereClause + " AND client_id IN :clientIds ";
                }
            default:
                break;

        }
        return getInvoicesUsingWhereClause(entityManager, whereClause, type, clientIds);
    }

    public BigDecimal getIncomeUsingWhereClause(EntityManager em, String whereClause, List<Long> clientIds) {
        Query query = null;

        query = em.createNativeQuery("SELECT sum(grand_total) FROM invoice WHERE " + whereClause);

        if (CollectionUtils.isNotEmpty(clientIds)) {
            query.setParameter("clientIds", clientIds);
        }
        return (BigDecimal) query.getSingleResult();

    }

    public BigDecimal prepareIncomeQuery(ZonedDateTime firstDate, ZonedDateTime lastDate, List<Long> clientIds) {

        String whereClause = "payment_status = 'PAID' ";

        if (firstDate == null && lastDate == null) {
            whereClause = "payment_status = 'PAID' ";
        } else if (firstDate != null && lastDate == null) {
            whereClause = whereClause + " And created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date < '" + firstDate.toLocalDateTime().plusDays(1) + "'";
        } else if (firstDate != null && lastDate != null) {
            whereClause = whereClause + " And created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date < '" + lastDate.toLocalDateTime() + "'";
        } else if (!lastDate.toLocalDateTime().equals(ZonedDateTime.now().toLocalDateTime())) {
            whereClause = whereClause + "created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date <= '" + lastDate.toLocalDateTime() + "'";
        }
        if (CollectionUtils.isNotEmpty(clientIds)) {
            whereClause = whereClause + " AND client_id IN :clientIds ";
        }

        return getIncomeUsingWhereClause(entityManager, whereClause, clientIds);
    }

    public BigDecimal getRefundUsingWhereClause(EntityManager em, String whereClause, List<Long> clientIds) {
        Query query = null;

        query = em.createNativeQuery(
            "SELECT sum(grand_total) " +
                " FROM invoice i " +
                " INNER JOIN payment p ON  p.invoice_id=i.id " +
                " INNER JOIN refund r ON r.payment_id = p.id " +
                ((StringUtils.isNotEmpty(whereClause)) ? " WHERE " + whereClause : ""));

        if (CollectionUtils.isNotEmpty(clientIds)) {
            query.setParameter("clientIds", clientIds);
        }
        return (BigDecimal) query.getSingleResult();

    }

    public BigDecimal prepareRefundQuery(StatisticsRequestDTO statisticsRequestDTO, List<Long> clientIds) {

        String whereClause = "";

        if (statisticsRequestDTO.getFromDate() != null && statisticsRequestDTO.getToDate() == null) {
            ZonedDateTime localFistDate = statisticsRequestDTO.getFromDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            whereClause = "i.created_date >= '" + localFistDate.toLocalDateTime();
        } else if (statisticsRequestDTO.getFromDate() != null && statisticsRequestDTO.getToDate() != null) {
            ZonedDateTime localFistDate = statisticsRequestDTO.getFromDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            ZonedDateTime localLastDate = statisticsRequestDTO.getToDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            whereClause = "i.created_date >= '" + localFistDate.toLocalDateTime() + "' AND i.created_date <= '" + localLastDate.toLocalDateTime() + "'";
        }

        if (CollectionUtils.isNotEmpty(clientIds)) {
            if (StringUtils.isNotEmpty(whereClause)) {
                whereClause = whereClause + " AND";
            }
            whereClause = whereClause + " client_id IN :clientIds ";
        }

        return getRefundUsingWhereClause(entityManager, whereClause, clientIds);
    }

}


