package sa.tamkeentech.tbs.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;

import java.io.Serializable;
import java.time.ZonedDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsRequestDTO  implements Serializable {

    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;

    private TypeStatistics type;
    private long clientId;

    private String offset;

}
