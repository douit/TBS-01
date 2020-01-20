package sa.tamkeentech.tbs.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import sa.tamkeentech.tbs.domain.enumeration.ReportStatus;
import sa.tamkeentech.tbs.domain.enumeration.ReportType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name="report")
@Data
@EqualsAndHashCode(of={"id"})
@SQLDelete(sql = "UPDATE report SET is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
public class Report extends AbstractAuditingEntity implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User requestUser;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client client;

    @Column(name = "report_type")
    private ReportType type;

    @Column(name = "request_date")
    private ZonedDateTime requestDate;

    @Column(name = "generated_date")
    private ZonedDateTime generatedDate;

    @Column(name = "expire_date")
    private ZonedDateTime expireDate;

    /*@Column(name = "download_url")
    private String downloadUrl;*/

	@Column(name = "report_status")
	private ReportStatus status;

	@Column(name = "off_set")
	private String offset;

    @JsonIgnore
    @Column(name = "is_deleted", columnDefinition = "boolean default false", nullable = false)
    private boolean deleted;

}
