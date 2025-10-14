package ru.t1.nour.microservice.util;

import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.security.UnsupportedKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class PublicKeyLocator extends LocatorAdapter<Key> {

    private final TrustedKeyStore trustedKeyStore;

    @Override
    public Key locate(JwsHeader header) {
        String keyId = header.getKeyId();
        if (keyId == null) {
            throw new UnsupportedKeyException("There is no 'kid' (Key ID) in the token header.");
        }

        return trustedKeyStore.getPublicKey(keyId)
                .orElseThrow(() -> new UnsupportedKeyException("The key for the kid was not found or is not trusted.:" + keyId));
    }
}
