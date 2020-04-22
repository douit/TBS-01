package sa.tamkeentech.tbs.service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.TypeStatistics;

import java.io.Serializable;
import java.time.ZonedDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSearchRequestDTO implements Serializable {

    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    private DataTablesInput input ;
    private long clientId;
    private long accountId;
//  private String customerId;
    private PaymentStatus paymentStatus;
}
