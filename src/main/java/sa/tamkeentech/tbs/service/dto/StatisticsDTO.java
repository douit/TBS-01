package sa.tamkeentech.tbs.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    private BigInteger totalInvoice;
    private BigInteger totalPaid;
    private BigDecimal amountRefund;
    private BigDecimal income;


}
