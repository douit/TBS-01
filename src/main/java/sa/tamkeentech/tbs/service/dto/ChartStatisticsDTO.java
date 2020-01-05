package sa.tamkeentech.tbs.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.ZonedDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartStatisticsDTO  implements Serializable {

    private TypeStatistics type;
    private ZonedDateTime duration;

    private BigInteger totalInvoice;

    private int month;
    private int day;
    private BigInteger totalPaid;

    private String offset;
}
