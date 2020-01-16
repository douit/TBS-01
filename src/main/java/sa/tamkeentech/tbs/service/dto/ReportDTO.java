package sa.tamkeentech.tbs.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sa.tamkeentech.tbs.domain.enumeration.ReportStatus;
import sa.tamkeentech.tbs.domain.enumeration.ReportType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A DTO for the Report entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO implements Serializable {

	private Long id;

	private Long requestUserId;

	private ReportType type;

	private ReportStatus status;

	private ZonedDateTime requestDate;

	private ZonedDateTime generatedDate;

	private ZonedDateTime expireDate;

	private String downloadUrl;

	private Long clientId;

    private String clientName;

    @NotNull
    private ZonedDateTime startDate;

    @NotNull
    private ZonedDateTime endDate;

}
