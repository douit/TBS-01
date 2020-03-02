package sa.tamkeentech.tbs.config;

import java.time.Duration;

import org.ehcache.config.builders.*;
import org.ehcache.jsr107.Eh107Configuration;

import org.hibernate.cache.jcache.ConfigSettings;
import io.github.jhipster.config.JHipsterProperties;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import sa.tamkeentech.tbs.repository.PaymentRepository;
import sa.tamkeentech.tbs.service.InvoiceService;

@Configuration
@EnableCaching
public class CacheConfiguration {

    private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

    public CacheConfiguration(JHipsterProperties jHipsterProperties) {
        JHipsterProperties.Cache.Ehcache ehcache = jHipsterProperties.getCache().getEhcache();

        jcacheConfiguration = Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class,
                ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build());
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return hibernateProperties -> hibernateProperties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer() {
        return cm -> {
            createCache(cm, sa.tamkeentech.tbs.repository.UserRepository.USERS_BY_LOGIN_CACHE);
            createCache(cm, sa.tamkeentech.tbs.repository.UserRepository.USERS_BY_EMAIL_CACHE);
            createCache(cm, PaymentRepository.PAYMENT_BY_TRANSACTION_ID);
            createCache(cm, InvoiceService.INVOICE_BY_ACCOUNT_ID);
            createCache(cm, sa.tamkeentech.tbs.domain.User.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Authority.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.User.class.getName() + ".authorities");
            createCache(cm, sa.tamkeentech.tbs.domain.PersistentToken.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.User.class.getName() + ".persistentTokens");
            createCache(cm, sa.tamkeentech.tbs.domain.Item.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Item.class.getName() + ".taxes");
            createCache(cm, sa.tamkeentech.tbs.domain.Tax.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Category.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Invoice.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Invoice.class.getName() + ".invoiceItems");
            createCache(cm, sa.tamkeentech.tbs.domain.Invoice.class.getName() + ".payments");
            createCache(cm, sa.tamkeentech.tbs.domain.InvoiceItem.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Discount.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Payment.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.PaymentMethod.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Refund.class.getName());
            createCache(cm, sa.tamkeentech.tbs.domain.Client.class.getName());
            // jhipster-needle-ehcache-add-entry
        };
    }

    private void createCache(javax.cache.CacheManager cm, String cacheName) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cm.destroyCache(cacheName);
        }
        cm.createCache(cacheName, jcacheConfiguration);
    }
}
