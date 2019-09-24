package sa.tamkeentech.tbs.security;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.service.ClientService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class APIKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final ClientService clientService;

    private String principalRequestHeader;

    public APIKeyAuthFilter(ClientService clientService, String principalRequestHeader) {
        this.clientService = clientService;
        this.principalRequestHeader = principalRequestHeader;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // return request.getHeader(principalRequestHeader);
        Optional<Client> client =  clientService.getClientBySecretKey(request.getHeader(principalRequestHeader));
        return client.map(Client::getClientId).orElse("");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
