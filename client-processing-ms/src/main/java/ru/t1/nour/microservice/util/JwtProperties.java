package ru.t1.nour.microservice.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "t1.app.security.jwt")
public class JwtProperties {
    // Spring автоматически свяжет 'private-key' из YAML с этим полем
    private String privateKey;

    // 'key-id' -> keyId
    private String keyId;

    // 'issuer-name' -> issuerName
    private String issuerName;

    // 'expiration-ms' -> expirationMs
    private long expirationMs;

    // --- САМОЕ ГЛАВНОЕ ---
    // Spring увидит свойство 'trusted-keys' в YAML и поймет,
    // что его нужно преобразовать в Map<String, String>.
    // Ключи из YAML станут ключами в Map, значения - значениями.
    private Map<String, String> trustedKeys;
}
