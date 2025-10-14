package ru.t1.nour.microservice.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.t1.nour.microservice.service.impl.UserDetailsServiceImpl;

import java.io.IOException;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        String jwt = null;
        String username = null;
        if (Objects.nonNull(authorizationHeader) &&
                authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // length of "Bearer "
            username = jwtUtils.extractUsername(jwt);
        }

        if (Objects.nonNull(username) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails =
                    this.userDetailsService.loadUserByUsername(username);
            boolean isTokenValidated =
                    jwtUtils.validateToken(jwt, userDetails);
            if (isTokenValidated) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(
                        usernamePasswordAuthenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

//    private String parseJwt(HttpServletRequest request) {
//        String headerAuth = request.getHeader("Authorization");
//
//        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
//            return headerAuth.substring(7);
//        }
//
//        return null;
//    }
}
