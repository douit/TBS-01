/**
 *
 */
package sa.tamkeentech.tbs.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Custom implementation of UserDetailsService.
 */
public class AttributesLDAPUserDetailsContextMapper extends LdapUserDetailsMapper {

	private final Logger log = LoggerFactory.getLogger(AttributesLDAPUserDetailsContextMapper.class);
	/**
	 * Attributes To Populate
	 */
	private String[] attributesToPopulate = new String[] {};
	/**
	 * Map All Attributes
	 */
	private boolean mapAllAttributes = true;

	@Inject
	private DomainUserDetailsService domainUserDetailsService;

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
		return domainUserDetailsService.loadUserByUsername(username);
	}

	/**
	 * @return the attributesToPopulate
	 */
	public String[] getAttributesToPopulate() {
		return attributesToPopulate;
	}

	/**
	 * @param attributesToPopulate
	 *            the attributesToPopulate to set
	 */
	public void setAttributesToPopulate(String[] attributesToPopulate) {
		this.attributesToPopulate = attributesToPopulate;
	}

	/**
	 * @return the mapAllAttributes
	 */
	public boolean isMapAllAttributes() {
		return mapAllAttributes;
	}

	/**
	 * @param mapAllAttributes
	 *            the mapAllAttributes to set
	 */
	public void setMapAllAttributes(boolean mapAllAttributes) {
		this.mapAllAttributes = mapAllAttributes;
	}

}
