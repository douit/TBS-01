package sa.tamkeentech.tbs.domain.ldap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;

@Entry(base = "OU=Tamkeen,OU=HDFBS,DC=HDFBS,DC=LOCAL", objectClasses = { "person", "top" })
@Data
public final class Person {

    private static final String BASE_DN = "OU=Tamkeen,OU=HDFBS,DC=HDFBS,DC=LOCAL";

    @Id
    @JsonIgnore
    private Name dn;

    @Attribute(name="uid")
    private String uid;

    @Attribute(name="sAMAccountName")
    private String userName;

    @Attribute(name="cn")
    private String fullName;

    @Attribute(name="givenName")
    private String firstName;

    @Attribute(name="sn")
    private String lastName;

    @DnAttribute(value="ou")
    @Transient
    private String group;

}
