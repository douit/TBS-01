package sa.tamkeentech.tbs.domain.ldap;

import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;

@Entry(base = "OU=Tamkeen,OU=HDFBS,DC=HDFBS,DC=LOCAL", objectClasses = { "person", "top" })
@Data
public final class Person {

    private static final String BASE_DN = "OU=Tamkeen,OU=HDFBS,DC=HDFBS,DC=LOCAL";

    @Id
    private Name dn;

    @Attribute(name="sAMAccountName")
    private String userName;

    @Attribute(name="cn")
    private String fullName;

    @Attribute(name="sn")
    private String lastName;

    @DnAttribute(value="ou")
    @Transient
    private String group;

}
