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
public class ChartStatisticsRequestDTO  implements Serializable {

    private ZonedDateTime date;
    private TypeStatistics type;



}
