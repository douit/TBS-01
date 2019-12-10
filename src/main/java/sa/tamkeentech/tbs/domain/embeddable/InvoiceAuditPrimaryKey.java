package sa.tamkeentech.tbs.domain.embeddable;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class InvoiceAuditPrimaryKey implements Serializable {

    @Column(name = "id")
    private Long id;

    @Column(name = "rev")
    private int revision;

}
