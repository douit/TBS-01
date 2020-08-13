package sa.tamkeentech.tbs.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import sa.tamkeentech.tbs.domain.Client;
import sa.tamkeentech.tbs.service.ClientService;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class APIKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

    private final ClientService clientService;

    private String principalRequestHeader;

    private final Logger log = LoggerFactory.getLogger(APIKeyAuthFilter.class);

    public APIKeyAuthFilter(ClientService clientService, String principalRequestHeader) {
        this.clientService = clientService;
        this.principalRequestHeader = principalRequestHeader;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // temp as TAHAQAQ client does not send
        String secretKey = request.getHeader(principalRequestHeader);
        log.debug("------Receiving consumer-secret: {}", secretKey);
        /*if (StringUtils.isEmpty(secretKey)) {
            secretKey = "06400E55EF3DD14AC6477E56681BF609B0A99B3620CB648CC4E8EF794F9C3AE8";
        }*/
        // Optional<Client> client =  clientService.getClientBySecretKey(request.getHeader(principalRequestHeader));
        // Optional<Client> client =  clientService.getClientBySecretKey("06400E55EF3DD14AC6477E56681BF609B0A99B3620CB648CC4E8EF794F9C3AE8");
        Optional<Client> client =  clientService.getClientBySecretKey(secretKey);
        return client.map(Client::getClientId).orElse("");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

}
