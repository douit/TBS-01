package sa.tamkeentech.tbs.config;

import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import sa.tamkeentech.tbs.service.util.CommonUtils;

@Component
@Slf4j
public class Initializr implements CommandLineRunner {

    @Autowired
    private Environment environment;

    @Value("${tbs.sentry-url}")
    private String sentryUrl;

	public void InitSentry() {
		// Config Sentry
        log.debug("Sentry url: {}", sentryUrl);
		Sentry.init(sentryUrl);
		Sentry.getContext().setUser(new UserBuilder().setUsername("Tamkeen-Billing-System").build());
		Sentry.getContext().addTag("thread", Thread.currentThread().getName());
		Sentry.getContext().addTag("version", "1.0");
		Sentry.getContext().addTag("servername", "Tamkeen-Billing-System");

	}

	public void captureError(String logError) {
		Sentry.capture(logError);
	}

    @Override
    public void run(String... strings) {
	    log.debug("---> start running the initializr");
	    if (CommonUtils.isProdOrStaging(environment)) {
            // InitSentry();
            // captureError("Test error sent to Sentry...");
        }
    }
}
