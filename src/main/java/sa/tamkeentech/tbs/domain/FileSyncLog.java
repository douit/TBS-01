package sa.tamkeentech.tbs.domain;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import sa.tamkeentech.tbs.domain.enumeration.IdentityType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A Customer.
 */
@Entity
@Table(name = "file_sync_log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class FileSyncLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "type")
    private String type;

    @Column(name = "date_created")
    private ZonedDateTime dateCreated;

    @Column(name = "date_executed")
    private ZonedDateTime dateExecuted;

    @Column(name = "successful")
    private boolean successful;

    @Column(name = "total_reconciled")
    private Integer totalReconciled;

}
