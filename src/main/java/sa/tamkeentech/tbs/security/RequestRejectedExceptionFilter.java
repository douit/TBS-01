package sa.tamkeentech.tbs.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRejectedExceptionFilter extends GenericFilterBean {

    private final Logger log = LoggerFactory.getLogger(RequestRejectedExceptionFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            RequestRejectedException requestRejectedException=(RequestRejectedException) servletRequest.getAttribute("isNormalized");
            if(Objects.nonNull(requestRejectedException)) {
                throw requestRejectedException;
            }else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } catch (RequestRejectedException requestRejectedException) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            log
                .error(
                    "request_rejected: remote={}, user_agent={}, request_url={}",
                    httpServletRequest.getRemoteHost(),
                    httpServletRequest.getHeader(HttpHeaders.USER_AGENT),
                    httpServletRequest.getRequestURL(),
                    requestRejectedException
                );
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpServletResponse.getWriter().write("");
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();

            // httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
