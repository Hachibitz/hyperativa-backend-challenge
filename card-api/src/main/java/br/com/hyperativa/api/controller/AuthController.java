package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.request.AuthRequestDto;
import br.com.hyperativa.api.model.dto.request.SignUpRequestDto;
import br.com.hyperativa.api.model.dto.response.AuthResponseDto;
import br.com.hyperativa.api.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Autentica um usuário", description = "Autentica um usuário com base em nome de usuário e senha, retornando um token JWT em caso de sucesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
    })
    @PostMapping
    @Loggable
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto authRequestDto) {
        return ResponseEntity.ok(authService.authenticate(authRequestDto));
    }

    @Operation(summary = "Registra um novo usuário (Apenas ADMIN)", description = "Cria um novo usuário no sistema. Este endpoint requer autenticação de um usuário com a role 'ADMIN'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de registro inválidos ou usuário já existente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Requer role de ADMIN", content = @Content)
    })
    @PostMapping("/signup")
    @Loggable
    public ResponseEntity<String> signUp(@Valid @RequestBody SignUpRequestDto signUpRequest) {
        authService.signUp(signUpRequest);
        return ResponseEntity.ok("User registered successfully!");
    }
}
