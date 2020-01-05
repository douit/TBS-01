package sa.tamkeentech.tbs.service;

import liquibase.database.core.FirebirdDatabase;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.tamkeentech.tbs.config.Constants;
import sa.tamkeentech.tbs.domain.PaymentMethod;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;
import sa.tamkeentech.tbs.service.dto.StatisticsRequestDTO;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    public List<Object[]> getInvoicesUsingWhereClause(EntityManager em, String whereClause, TypeStatistics type) {
        Query query = null;

        switch (type) {
            case GENERAL:
                if (whereClause == "") {
                    query = em.createNativeQuery(
                        "SELECT count(*) As totalInvoice , \n " +
                            "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                            "      FROM invoice ");
                } else {
                    query = em.createNativeQuery(
                        "SELECT count(*) As totalInvoice , \n " +
                            "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                            "      FROM invoice WHERE " +
                            whereClause);
                }

                break;
            case ANNUAL:
                query = em.createNativeQuery(
                    "SELECT date_trunc('month', created_date) As Month , count(*) As totalInvoice , \n " +
                        "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                        "      FROM invoice WHERE " +
                        whereClause +
                        "      group by Month ORDER BY Month");
                break;
            case MONTHLY:
                query = em.createNativeQuery(
                    "SELECT date_trunc('day', created_date) As Day , count(*) As totalInvoice , \n " +
                        "      sum(case when payment_status = 'PAID' then 1 else 0 end ) As PaidInvoice" +
                        "      FROM invoice WHERE " +
                        whereClause +
                        "      group by Day ORDER BY Day"
                );
                break;
            default:
                break;
        }
        return query.getResultList();

    }

    public List<Object[]> prepareQuery(ZonedDateTime firstDate, ZonedDateTime lastDate, long clientId, TypeStatistics type) {
        ZonedDateTime first = null;
        ZonedDateTime last = null;
        YearMonth yearMonthObject = null;

        String whereClause = null;
        switch (type) {
            case GENERAL:
                continue testing other types and do same toLocalDateTime
                if (firstDate == null && lastDate == null) {
                    whereClause = "";
                } else if (firstDate != null && lastDate == null) {
                    whereClause = "created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date < '" + firstDate.toLocalDateTime().plusDays(1) + "'";
                } else if (firstDate != null && lastDate != null) {
                    whereClause = "created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date <= '" + lastDate.toLocalDateTime() + "'";

                } else if (!lastDate.toLocalDateTime().equals(ZonedDateTime.now().toLocalDateTime())) {
                    whereClause = "created_date >= '" + firstDate.toLocalDateTime() + "' AND created_date <= '" + lastDate.toLocalDateTime() + "'";
                }
                if (clientId != 0) {
                    if (StringUtils.isEmpty(whereClause)) {
                        whereClause = " client_id = '" + clientId + "'";
                    } else {
                        whereClause = whereClause + " AND client_id = '" + clientId + "'";
                    }
                }
                break;
            case ANNUAL:
                first = firstDate.withMonth(1);
                last = firstDate.withMonth(12);
                if (first != null && last != null) {
                    whereClause = "created_date >= '" + first.toLocalDate() + "' AND created_date <= '" + last.toLocalDate() + "'";

                }
                if (clientId != 0) {
                    whereClause = whereClause + " AND client_id = '" + clientId + "'";
                }
                break;
            case MONTHLY:
                first = firstDate.withDayOfMonth(1);
                 yearMonthObject = YearMonth.of(firstDate.getYear(), firstDate.getMonth());
                last = firstDate.withDayOfMonth(yearMonthObject.lengthOfMonth());

                if (first != null && last != null) {
                    whereClause = "created_date >= '" + first.toLocalDate() + "' AND created_date <= '" + last.toLocalDate() + "'";

                }
                if (clientId != 0) {
                    whereClause = whereClause + " AND client_id = '" + clientId + "'";
                }
            default:
                break;

        }
        return getInvoicesUsingWhereClause(entityManager, whereClause, type);
    }

    public BigDecimal getIncomeUsingWhereClause(EntityManager em, String whereClause) {
        Query query = null;

        query = em.createNativeQuery(
            "SELECT sum(grand_total)   \n " +
                "      FROM invoice  WHERE " +
                whereClause);

        return (BigDecimal) query.getSingleResult();

    }

    public BigDecimal prepareIncomeQuery(ZonedDateTime firstDate, ZonedDateTime lastDate, long clientId) {

        String whereClause = "payment_status = 'PAID' ";

        if (firstDate == null && lastDate == null) {
            whereClause = "payment_status = 'PAID' ";
        } else if (firstDate != null && lastDate == null) {
            whereClause = whereClause + " And created_date >= '" + firstDate.toLocalDate() + "' AND created_date < '" + firstDate.toLocalDate().plusDays(1) + "'";
        } else if (firstDate != null && lastDate != null) {
            whereClause = whereClause + " And created_date >= '" + firstDate.toLocalDate() + "' AND created_date < '" + lastDate.toLocalDate() + "'";
        } else if (!lastDate.toLocalDate().equals(ZonedDateTime.now().toLocalDate())) {
            whereClause = whereClause + "created_date >= '" + firstDate.toLocalDate() + "' AND created_date <= '" + lastDate.toLocalDate() + "'";
        }
        if (clientId != 0) {
            whereClause = whereClause + " AND client_id = '" + clientId + "'";
        }

        return getIncomeUsingWhereClause(entityManager, whereClause);
    }

    public BigDecimal getRefundUsingWhereClause(EntityManager em, String whereClause) {
        Query query = null;

        query = em.createNativeQuery(
            "SELECT sum(grand_total) " +
                " FROM invoice i " +
                " INNER JOIN payment p ON  p.invoice_id=i.id " +
                " INNER JOIN refund r ON r.payment_id = p.id " +
                ((StringUtils.isNotEmpty(whereClause))? " WHERE " + whereClause: ""));

        return (BigDecimal) query.getSingleResult();

    }

    public BigDecimal prepareRefundQuery(StatisticsRequestDTO statisticsRequestDTO, long clientId) {

        String whereClause = "";

        if (statisticsRequestDTO.getFromDate() != null && statisticsRequestDTO.getToDate() == null) {
            ZonedDateTime localFistDate = statisticsRequestDTO.getFromDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            whereClause = "i.created_date >= '" + localFistDate.toLocalDate();
        } else if (statisticsRequestDTO.getFromDate() != null && statisticsRequestDTO.getToDate() != null) {
            ZonedDateTime localFistDate = statisticsRequestDTO.getFromDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            ZonedDateTime localLastDate = statisticsRequestDTO.getToDate().withZoneSameLocal(Constants.UTC_ZONE_ID).withZoneSameInstant(ZoneId.ofOffset("UTC", ZoneOffset.of(statisticsRequestDTO.getOffset())));
            whereClause = "i.created_date >= '" + localFistDate.toLocalDate() + "' AND i.created_date <= '" + localLastDate.toLocalDate() + "'";
        }

        if (clientId != 0) {
            if (StringUtils.isNotEmpty(whereClause)) {
                whereClause = whereClause + " AND";
            }
            whereClause = whereClause + " client_id = '" + clientId + "'";
        }

        return getRefundUsingWhereClause(entityManager, whereClause);
    }

}


