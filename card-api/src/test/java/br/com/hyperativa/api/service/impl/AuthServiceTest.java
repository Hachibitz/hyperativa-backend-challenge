package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.model.dto.request.AuthRequestDto;
import br.com.hyperativa.api.model.dto.request.SignUpRequestDto;
import br.com.hyperativa.api.model.dto.response.AuthResponseDto;
import br.com.hyperativa.api.model.entity.Role;
import br.com.hyperativa.api.model.entity.User;
import br.com.hyperativa.api.model.enums.RoleEnum;
import br.com.hyperativa.api.repository.RoleRepository;
import br.com.hyperativa.api.repository.UserRepository;
import br.com.hyperativa.api.security.CustomUserDetails;
import br.com.hyperativa.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName(RoleEnum.ROLE_USER);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .roles(Set.of(testRole))
                .build();
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar um token JWT")
    void authenticate_WithValidCredentials_ShouldReturnAuthResponse() {
        AuthRequestDto request = AuthRequestDto.builder()
                .username("testuser")
                .password("password")
                .build();
        UserDetails userDetails = new CustomUserDetails(testUser);
        String expectedToken = "dummy.jwt.token";

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(expectedToken);

        AuthResponseDto response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso")
    void signUp_WithNewUsername_ShouldSaveUser() {
        SignUpRequestDto request = SignUpRequestDto.builder()
                .username("newuser")
                .password("password123")
                .build();
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleEnum.ROLE_USER)).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        authService.signUp(request);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(request.getUsername(), savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(testRole));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar um usuário com username já existente")
    void signUp_WithExistingUsername_ShouldThrowIllegalArgumentException() {
        SignUpRequestDto request = SignUpRequestDto.builder()
                .username("testuser")
                .password("password123")
                .build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(request);
        });

        assertEquals("Username already in use.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar se a role padrão não for encontrada")
    void signUp_WhenDefaultRoleNotFound_ShouldThrowRuntimeException() {
        SignUpRequestDto request = SignUpRequestDto.builder()
                .username("newuser")
                .password("password123")
                .build();
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleEnum.ROLE_USER)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.signUp(request);
        });

        assertEquals("Error: Default role (ROLE_USER) not found in database.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}