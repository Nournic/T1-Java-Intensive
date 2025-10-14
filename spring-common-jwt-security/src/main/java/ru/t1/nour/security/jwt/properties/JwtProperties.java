package ru.t1.nour.security.jwt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "t1.app.security.jwt")
public class JwtProperties {

    private String privateKey;

    private String keyId;

    private String issuerName;

    private long expirationMs;

    private Map<String, String> trustedPublicKeys;
}
