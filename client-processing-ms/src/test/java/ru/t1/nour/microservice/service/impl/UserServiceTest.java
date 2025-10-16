package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.t1.nour.microservice.model.Role;
import ru.t1.nour.microservice.model.User;
import ru.t1.nour.microservice.model.dto.request.AuthRequest;
import ru.t1.nour.microservice.model.dto.response.JwtResponse;
import ru.t1.nour.microservice.model.enums.RoleEnum;
import ru.t1.nour.microservice.repository.RoleRepository;
import ru.t1.nour.microservice.repository.UserRepository;
import ru.t1.nour.security.jwt.JwtUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository; // Не используется в authorize, но нужен для создания сервиса
    @Mock
    private RoleRepository roleRepository; // То же самое
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock
    private PasswordEncoder encoder; // Не используется, но нужен

    // --- Мокаем объект Authentication, который вернет AuthenticationManager ---
    @Mock
    private Authentication authentication;

    // --- Тестируемый сервис ---
    @InjectMocks
    private UserService userService;

    @Test
    void should_returnJwtResponse_when_authenticationIsSuccessful() {
        // --- ARRANGE (Подготовка) ---

        // 1. Входные данные
        var authRequest = new AuthRequest("testuser", "password123");
        String expectedJwt = "generated.jwt.token";

        // 2. Создаем "пользователя", которого якобы найдет UserDetailsService
        Set<Role> authorities = Set.of(new Role(RoleEnum.USER_ROLE));
        UserDetails userDetails = new User(authRequest.getUsername(), "", authRequest.getPassword(), authorities);

        // 3. "Обучаем" моки
        // "Когда кто-то вызовет authenticate у authenticationManager с ЛЮБЫМ токеном,
        // верни наш заранее созданный мок-объект authentication"
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // "Когда кто-то вызовет loadUserByUsername, верни нашего userDetails"
        when(userDetailsServiceImpl.loadUserByUsername(authRequest.getUsername()))
                .thenReturn(userDetails);

        // "Когда кто-то вызовет generateJwtToken, верни нашу тестовую строку токена"
        when(jwtUtils.generateJwtToken(any(Map.class), eq(authRequest.getUsername())))
                .thenReturn(expectedJwt);


        // --- ACT (Действие) ---

        ResponseEntity<?> responseEntity = userService.authorize(authRequest);


        // --- ASSERT (Проверка) ---

        // 1. Проверяем HTTP-ответ
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Проверяем тело ответа
        assertThat(responseEntity.getBody()).isInstanceOf(JwtResponse.class);
        JwtResponse jwtResponse = (JwtResponse) responseEntity.getBody();
        assertThat(jwtResponse.getToken()).isEqualTo(expectedJwt);
    }

    // Тест на неудачную аутентификацию (опционально)
    @Test
    void should_rethrowException_when_authenticationFails() {
        // --- ARRANGE ---
        var authRequest = new AuthRequest("wronguser", "wrongpassword");

        // "Обучаем" authenticationManager выбрасывать исключение
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> userService.authorize(authRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Bad credentials");
    }
}
