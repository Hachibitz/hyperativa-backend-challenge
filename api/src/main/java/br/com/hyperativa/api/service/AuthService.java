package br.com.hyperativa.api.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            @Lazy AuthenticationManager authenticationManager,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDto authenticate(AuthRequestDto request) {
        log.info("Attempting to authenticate user: {}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userRepository.findByUsername(request.getUsername())
                .map(CustomUserDetails::new)
                .orElseThrow();
        String token = jwtService.generateToken(userDetails);
        log.info("User {} authenticated successfully.", request.getUsername());
        return new AuthResponseDto(token);
    }

    public void signUp(SignUpRequestDto signUpRequest) {
        log.info("Attempting to sign up new user with username: {}", signUpRequest.getUsername());
        if (userRepository.findByUsername(signUpRequest.getUsername()).isPresent() || userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            log.warn("Sign up failed for user {}: username or email already in use.", signUpRequest.getUsername());
            throw new IllegalArgumentException("Username or Email already in use.");
        }

        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Default role (ROLE_USER) not found in database."));

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        log.info("User {} registered successfully with ID: {}", user.getUsername(), user.getId());
    }
}
