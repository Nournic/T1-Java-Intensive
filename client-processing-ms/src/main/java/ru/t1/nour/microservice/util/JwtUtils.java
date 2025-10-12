package ru.t1.nour.microservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.service.impl.UserDetailsImpl;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils {

    @Value("${security.token}")
    private String jwtSecret;

    @Value("${security.expiration}")
    private int jwtExpirationMs;

    @Value("${t1.app.starter.app-name}")
    private String APP_NAME;

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .issuer(APP_NAME)
                .subject((userPrincipal.getUsername()))
                .issuedAt(java.util.Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS)))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));}

    public String getUserNameFromJwtToken(String token) {
        return parseJwt(token)
                .getPayload().getSubject();
    }

    public Jws<Claims> parseJwt(String jwtString){
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(jwtString);
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}

