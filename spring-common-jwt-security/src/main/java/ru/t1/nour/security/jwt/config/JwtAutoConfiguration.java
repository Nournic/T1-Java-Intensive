package ru.t1.nour.security.jwt.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.nour.security.jwt.AuthAccessDeniesHandler;
import ru.t1.nour.security.jwt.AuthEntryPointJwt;
import ru.t1.nour.security.jwt.JwtUtils;
import ru.t1.nour.security.jwt.properties.JwtProperties;
import ru.t1.nour.security.jwt.utils.PublicKeyLocator;
import ru.t1.nour.security.jwt.utils.TrustedKeyStore;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TrustedKeyStore trustedKeyStore(JwtProperties jwtProperties) {
        return new TrustedKeyStore(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PublicKeyLocator publicKeyLocator(TrustedKeyStore trustedKeyStore) {
        return new PublicKeyLocator(trustedKeyStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties properties, PublicKeyLocator publicKeyLocator) {
        return new JwtUtils(properties, publicKeyLocator);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthEntryPointJwt authEntryPointJwt() {
        return new AuthEntryPointJwt();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthAccessDeniesHandler accessDeniedHandler(){
        return new AuthAccessDeniesHandler();
    }
}
