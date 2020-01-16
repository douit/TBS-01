package sa.tamkeentech.tbs.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO implements Serializable {


	private Long clientId;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private String offset;

}
