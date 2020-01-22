package sa.tamkeentech.tbs.repository.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import sa.tamkeentech.tbs.domain.ldap.Person;
import java.util.List;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class PersonRepository {

    @Autowired
    private LdapTemplate ldapTemplate;

    public Person findByUid(String uid) {
        return ldapTemplate.findOne(query().where("uid").is(uid), Person.class);
    }

    public Person findByUserName(String userName) {
        return ldapTemplate.findOne(query().where("sAMAccountName").is(userName), Person.class);
    }

    public List<Person> findByLastName(String lastName) {
        return ldapTemplate.find(query().where("sn").is(lastName), Person.class);
    }
}
