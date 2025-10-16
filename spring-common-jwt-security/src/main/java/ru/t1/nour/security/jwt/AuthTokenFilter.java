package ru.t1.nour.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;


@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Optional<String> jwtOptional = parseJwt(request);

            if (jwtOptional.isPresent()) {
                String jwt = jwtOptional.get();

                Optional<Authentication> authOptional = jwtUtils.getAuthentication(jwt);

                if (authOptional.isPresent())
                    SecurityContextHolder.getContext().setAuthentication(authOptional.get());

            }
        } catch (Exception e) {
            log.error("User authentication could not be established: {}", e.getMessage());
        }

        // 4. Продолжаем выполнение цепочки фильтров в любом случае
        filterChain.doFilter(request, response);
    }

    private Optional<String> parseJwt(HttpServletRequest request) {
        final String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer "))
            return Optional.of(headerAuth.substring(7));

        return Optional.empty();
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
