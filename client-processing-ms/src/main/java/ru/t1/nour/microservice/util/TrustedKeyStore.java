package ru.t1.nour.microservice.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrustedKeyStore {

    private final JwtProperties jwtProperties;
    private final Map<String, PublicKey> publicKeyCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        if(jwtProperties.getTrustedKeys() != null){
            jwtProperties.getTrustedKeys().forEach((kid, str)->{
                try{
                    publicKeyCache.put(kid, convertStringToPublicKey(str));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to convert public key for kid: " + kid, e);
                }
            });
        }

    }

    public Optional<PublicKey> getPublicKey(String kid) {
        return Optional.ofNullable(publicKeyCache.get(kid));
    }

    private PublicKey convertStringToPublicKey(String keyStr) throws Exception {
        String publicKeyPEM = keyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }
}
