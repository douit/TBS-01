package sa.tamkeentech.tbs.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    private long numInvoice;
    private long numPaid;
    private BigDecimal amountRefund;
    private BigDecimal income;


}
