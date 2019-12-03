package sa.tamkeentech.tbs.config;

import io.github.jhipster.config.JHipsterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.ZonedDateTime;
import java.util.Optional;


@Configuration
@EnableJpaRepositories("sa.tamkeentech.tbs.repository")
// @EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableTransactionManagement
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean // Makes ZonedDateTime compatible with auditing fields
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(ZonedDateTime.now());
    }
}
