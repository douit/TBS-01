package sa.tamkeentech.tbs.domain;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

import sa.tamkeentech.tbs.domain.enumeration.DateUnit;

/**
 * A Client.
 */
@Entity
@Table(name = "client")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   /* @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    @SequenceGenerator(name = "sequence_generator", sequenceName="sequence_generator")*/
    private Long id;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "name")
    private String name;

    @Column(name = "logo")
    private String logo;

    @Enumerated(EnumType.STRING)
    @Column(name = "due_date_unit")
    private DateUnit dueDateUnit;

    @Column(name = "due_date_value")
    private Integer dueDateValue;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "payment_key_app")
    private String paymentKeyApp;

    @Column(name = "client_token")
    private String clientToken;

    @Column(name = "token_expiry_date")
    private ZonedDateTime tokenExpiryDate;

    @Column(name = "notification_url")
    private String notificationUrl;

    @Column(name = "initial_account_id")
    private Long initialAccountId;

    @Column(name = "initial_bill_id")
    private Long initialBillId;

    @Column(name = "redirect_url")
    private String redirectUrl;

}
