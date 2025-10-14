package ru.t1.nour.microservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.io.IOException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    private final PrivateKey privateKey;
    private final String keyId;
    private final String issuerName;
    private final long jwtExpirationMs;
    private final JwtParser jwtParser;

    public JwtUtils(JwtProperties jwtProperties, PublicKeyLocator publicKeyLocator) {
        this.privateKey = getPrivateKeyFromString(jwtProperties.getPrivateKey());
        this.keyId = jwtProperties.getKeyId();
        this.issuerName = jwtProperties.getIssuerName();
        this.jwtExpirationMs = jwtProperties.getExpirationMs();
        this.jwtParser = Jwts.parser()
                            .keyLocator(publicKeyLocator)
                            .build();
    }

    public String generateJwtToken(Map<String, Object> claims, String subject) {
        if(privateKey == null)
            throw new RuntimeException("The private key has not been initialized");

        return Jwts.builder()
                .issuer(issuerName)
                .header().keyId(keyId).and()
                .claims()
                    .add(claims)
                    .and()
                .subject(subject)
                .issuedAt(java.util.Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS)))
                .signWith(privateKey)
                .compact();
    }

    public Optional<Authentication> getAuthentication(String token) {
        try {
            Jws<Claims> jwsClaims = this.jwtParser.parseSignedClaims(token);
            Claims claims = jwsClaims.getPayload();

            String subject = claims.getSubject();
            if (subject == null)
                return Optional.empty();

            return Optional.of(new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList()));

        } catch (ExpiredJwtException e) {
            log.warn("The token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Incorrect token format: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Signature verification error: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Empty token or invalid argument: {}", e.getMessage());
        } catch (IOException e) {
            log.warn("Couldn't parse the payload of the token: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public Jws<Claims> extractClaims(String bearerToken) {
        return this.jwtParser.parseSignedClaims(bearerToken);
    }

    public <T> T extractClaimBody(String bearerToken,
                                  Function<Claims, T> claimsResolver) {
        Jws<Claims> jwsClaims = extractClaims(bearerToken);
        return claimsResolver.apply(jwsClaims.getPayload());
    }

    public String extractUsername(String bearerToken) {
        return extractClaimBody(bearerToken, Claims::getSubject);
    }

    public boolean isTokenExpired(String bearerToken) {
        return extractExpiry(bearerToken).before(new Date());
    }
    public Date extractExpiry(String bearerToken) {
        return extractClaimBody(bearerToken, Claims::getExpiration);
    }

//    public Jws<Claims> parseJwt(String jwtString){
//        return Jwts.parser()
//                .verifyWith(key())
//                .build()
//                .parseSignedClaims(jwtString);
//    }
//
//    public boolean validateJwtToken(String authToken) {
//        try {
//            Jwts.parser()
//                    .verifyWith(key())
//                    .build()
//                    .parse(authToken);
//            return true;
//        } catch (MalformedJwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//        } catch (ExpiredJwtException e) {
//            log.error("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            log.error("JWT token is unsupported: {}", e.getMessage());
//        } catch (IllegalArgumentException e) {
//            log.error("JWT claims string is empty: {}", e.getMessage());
//        }
//        return false;
//    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUsername(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public PrivateKey getPrivateKeyFromString(String key) {
        try {
            byte[] encoded = Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Error when converting a private key", e);
        }
    }
}

