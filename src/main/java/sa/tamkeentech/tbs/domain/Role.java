package sa.tamkeentech.tbs.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "role",
        uniqueConstraints=
        @UniqueConstraint(columnNames={"id"}))
//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role implements Serializable{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "role_authority",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_name", referencedColumnName = "name"),
            uniqueConstraints=
                @UniqueConstraint(columnNames={"role_id", "authority_name"})
    )
    private Set<Authority> authorities;

}
