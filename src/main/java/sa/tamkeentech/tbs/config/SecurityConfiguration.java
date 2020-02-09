package sa.tamkeentech.tbs.config;

import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.security.AjaxAuthenticationFailureHandler;
import io.github.jhipster.security.AjaxAuthenticationSuccessHandler;
import io.github.jhipster.security.AjaxLogoutSuccessHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import sa.tamkeentech.tbs.security.APIKeyAuthFilter;
import sa.tamkeentech.tbs.security.AttributesLDAPUserDetailsContextMapper;
import sa.tamkeentech.tbs.security.AuthoritiesConstants;
import sa.tamkeentech.tbs.security.DomainUserDetailsService;
import sa.tamkeentech.tbs.service.ClientService;

import javax.inject.Inject;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    @Configuration
    @Order(1)
    public static class AppWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        @Value("${tbs.http.token-header-name}")
        private String principalRequestHeader;
        private final ClientService clientService;

        public AppWebSecurityConfiguration(ClientService clientService) {
            this.clientService = clientService;
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            APIKeyAuthFilter filter = new APIKeyAuthFilter(clientService, principalRequestHeader);
            filter.setAuthenticationManager(new AuthenticationManager() {
                @Override
                public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                    String principal = (String) authentication.getPrincipal();
                    if (StringUtils.isEmpty(principal))
                    {
                        throw new BadCredentialsException("The API key was not found or not the expected value.");
                    }
                    authentication.setAuthenticated(true);
                    return authentication;
                }
            });

            httpSecurity.
                antMatcher("/billing/**").// change this to invoicesApp ToDO !!!!
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilter(filter).authorizeRequests().anyRequest().authenticated();
        }
    }

    @Configuration
    public static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private final JHipsterProperties jHipsterProperties;

        private final RememberMeServices rememberMeServices;

        private final CorsFilter corsFilter;
        private final SecurityProblemSupport problemSupport;

        private final DomainUserDetailsService domainUserDetailsService;

        private final Environment env;

        public WebSecurityConfiguration(JHipsterProperties jHipsterProperties, RememberMeServices rememberMeServices, CorsFilter corsFilter, SecurityProblemSupport problemSupport, DomainUserDetailsService domainUserDetailsService, Environment env) {
            this.jHipsterProperties = jHipsterProperties;
            this.rememberMeServices = rememberMeServices;
            this.corsFilter = corsFilter;
            this.problemSupport = problemSupport;
            this.domainUserDetailsService = domainUserDetailsService;
            this.env = env;
        }

        @Bean
        public AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
            return new AjaxAuthenticationSuccessHandler();
        }

        @Bean
        public AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
            return new AjaxAuthenticationFailureHandler();
        }

        @Bean
        public AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler() {
            return new AjaxLogoutSuccessHandler();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/i18n/**")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui/index.html")
                .antMatchers("/test/**")
                .antMatchers("/h2-console/**")
                .antMatchers("/sadad/paymentnotification**")
                .antMatchers("/billing/payments/credit-card");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
                .addFilterBefore(corsFilter, CsrfFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
            .and()
                .rememberMe()
                .rememberMeServices(rememberMeServices)
                .rememberMeParameter("remember-me")
                .key(jHipsterProperties.getSecurity().getRememberMe().getKey())
            .and()
                .formLogin()
                .loginProcessingUrl("/api/authentication")
                .successHandler(ajaxAuthenticationSuccessHandler())
                .failureHandler(ajaxAuthenticationFailureHandler())
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(ajaxLogoutSuccessHandler())
                .permitAll()
            .and()
                .headers()
                .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' https://fonts.googleapis.com 'unsafe-inline'; img-src 'self' data:; font-src 'self' https://fonts.gstatic.com data:")
                //.contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
            .and()
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
                .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
            .and()
                .frameOptions()
                .deny()
            .and()
                .authorizeRequests()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/activate").permitAll()
                .antMatchers("/api/account/reset-password/init").permitAll()
                .antMatchers("/api/account/reset-password/finish").permitAll()
                .antMatchers("/api/categories").permitAll()
                .antMatchers("/api/clients/role").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/info").permitAll()
                .antMatchers("/management/prometheus").permitAll()
                .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
            // @formatter:on
        }

        // LDAP
        @Inject
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(domainUserDetailsService).passwordEncoder(passwordEncoder())
                .and()
                .ldapAuthentication()
                .userDetailsContextMapper(userDetailsContextMapper())
                .userSearchBase("") //don't add the base
                .userSearchFilter("(sAMAccountName={0})")
                // .groupSearchBase("ou=Groups") //don't add the base
                // .groupSearchFilter("member={0}")
                .contextSource(getContextSource());
        }

        @Bean
        public LdapContextSource getContextSource() {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrl(env.getProperty("spring.ldap.urls"));
            contextSource.setBase(env.getProperty("spring.ldap.base"));
            contextSource.setUserDn(env.getProperty("spring.ldap.username"));
            contextSource.setPassword(env.getProperty("spring.ldap.password"));
            contextSource.afterPropertiesSet(); //needed otherwise you will have a NullPointerException in spring

            return contextSource;
        }

        @Bean
        public AttributesLDAPUserDetailsContextMapper userDetailsContextMapper() {
            return new AttributesLDAPUserDetailsContextMapper();
        }

    }
}
