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
        // temp as client does not send
        // Optional<Client> client =  clientService.getClientBySecretKey(request.getHeader(principalRequestHeader));
        Optional<Client> client =  clientService.getClientBySecretKey("06400E55EF3DD14AC6477E56681BF609B0A99B3620CB648CC4E8EF794F9C3AE8");
        return client.map(Client::getClientId).orElse("");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
