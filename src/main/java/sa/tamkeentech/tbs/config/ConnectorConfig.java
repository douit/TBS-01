package sa.tamkeentech.tbs.config;

import io.undertow.UndertowOptions;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.TransportGuaranteeType;
import io.undertow.servlet.api.WebResourceCollection;

import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;


@Configuration
public class ConnectorConfig {

    @Value("${server.ssl.key-store}")
    private String keyStoreLocation;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${server.ssl.key-password}")
    private String keyPassword;

    @Bean
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> containerCustomizer() {
        return (WebServerFactoryCustomizer) factory -> {
            UndertowServletWebServerFactory undertowFactory = (UndertowServletWebServerFactory) factory;
            undertowFactory.getBuilderCustomizers().add(builder -> {
                try {
                    SSLContext sslContext = SSLContextBuilder
                        .create()
                        .loadKeyMaterial(ResourceUtils.getFile(keyStoreLocation), keyStorePassword.toCharArray(), keyPassword.toCharArray())
                        .build();

                    builder.addHttpsListener(443, "0.0.0.0", sslContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };
    }

    /*@Bean
    public UndertowServletWebServerFactory embeddedServletContainerFactory() {

        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();

        factory.addBuilderCustomizers((UndertowBuilderCustomizer) builder -> {
           builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
           builder.addHttpListener(443, "0.0.0.0");
           builder.addHttpListener(80, "0.0.0.0");
        });

        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            deploymentInfo.addSecurityConstraint(
                new SecurityConstraint()
                    .addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*"))
                    .setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
                    .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
                .setConfidentialPortManager(exchange -> 8081);
        });

        return factory;
    }*/


/*
    public SSLContext getSSLContext() throws Exception
    {
        *//*
         return createSSLContext(loadKeyStore(serverKeystore,keyStorePassword),
            loadKeyStore(serverTruststore,trustStorePassword));
        * *//*
        return createSSLContext(loadKeyStore("tls/billing.tamkeentech.sa.jks", "tm1548pmHea92"),
            loadKeyStore("tls/tam20-20.pfx", "12345"));

    }


    private SSLContext createSSLContext(final KeyStore keyStore,
                                        final KeyStore trustStore) throws Exception {

        KeyManager[] keyManagers;
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "tm1548pmHea92".toCharArray());
        keyManagers = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        trustManagers = trustManagerFactory.getTrustManagers();

        SSLContext sslContext;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        return sslContext;
    }


    private static KeyStore loadKeyStore(final String storeLoc, final String storePw) throws Exception {
        InputStream stream = Files.newInputStream(Paths.get(storeLoc));
        if(stream == null) {
            throw new IllegalArgumentException("Could not load keystore");
        }
        try(InputStream is = stream) {
            KeyStore loadedKeystore = KeyStore.getInstance("JKS");
            loadedKeystore.load(is, storePw.toCharArray());
            return loadedKeystore;
        }
    }*/


}
