package ru.t1.nour.security.jwt.utils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.t1.nour.security.jwt.properties.JwtProperties;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        byte[] encoded = Base64.getDecoder().decode(keyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }
}
