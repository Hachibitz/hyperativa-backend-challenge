package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.request.AuthRequestDto;
import br.com.hyperativa.api.model.dto.request.SignUpRequestDto;
import br.com.hyperativa.api.model.dto.response.AuthResponseDto;
import br.com.hyperativa.api.service.impl.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    @Loggable
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto authRequestDto) {
        return ResponseEntity.ok(authService.authenticate(authRequestDto));
    }

    @PostMapping("/signup")
    @Loggable
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequestDto signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.ok("User registered successfully!");
    }
}
