package sa.tamkeentech.tbs.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A RoleUser.
 */
@Entity
@Table(name = "user_role")
@EqualsAndHashCode(of = {"role", "client"})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserRole extends AbstractAuditingEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    @ToString.Exclude
    private User user;

    @Column(name = "activated")
    private Boolean activated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="client_id")
    private Client client;

}
