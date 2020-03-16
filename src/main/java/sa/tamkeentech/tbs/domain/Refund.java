package sa.tamkeentech.tbs.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

import sa.tamkeentech.tbs.domain.enumeration.PaymentStatus;
import sa.tamkeentech.tbs.domain.enumeration.RequestStatus;

/**
 * A Refund.
 */
@Entity
@Table(name = "refund")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Refund extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /*@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator", sequenceName="sequence_generator")*/
    private Long id;

    /*@Column(name = "amount", precision = 21, scale = 2)
    private BigDecimal amount;*/

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    /*@Column(name = "refund_id")
    private String refundId;*/

    @Column(name = "biller_id")
    private String billerId;

    @Column(name = "bank_id")
    private String bankId;

    // official id of the owner who paid the bill
    @Column(name = "official_id")
    private String officialId;

    @ManyToOne
    @JsonIgnoreProperties("refunds")
    private Payment payment;


    @Column(name = "refund_value")
    private BigDecimal refundValue;


}
