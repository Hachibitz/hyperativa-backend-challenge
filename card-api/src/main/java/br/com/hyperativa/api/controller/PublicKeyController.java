package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.request.EncryptCardRequestDto;
import br.com.hyperativa.api.model.dto.response.EncryptCardResponseDto;
import br.com.hyperativa.api.service.impl.RsaEncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/security")
@Tag(name = "Security", description = "Endpoints relacionados à segurança da API")
public class PublicKeyController {

    private final PublicKey publicKey;
    private final RsaEncryptionService rsaEncryptionService;

    public PublicKeyController(PublicKey publicKey, RsaEncryptionService rsaEncryptionService) {
        this.publicKey = publicKey;
        this.rsaEncryptionService = rsaEncryptionService;
    }

    @Operation(
            summary = "Obtém a chave pública da API",
            description = "Retorna a chave pública RSA (em formato Base64) a ser usada para a criptografia End-to-End dos dados sensíveis."
    )
    @ApiResponse(responseCode = "200", description = "Chave pública retornada com sucesso")
    @GetMapping("/public-key")
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    @Operation(summary = "Criptografa um número de cartão", description = "Recebe um número de cartão e retorna o valor criptografado com a chave pública da API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Número criptografado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EncryptCardResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    @PostMapping("/encrypt-card")
    @Loggable
    public ResponseEntity<EncryptCardResponseDto> encryptCard(@Valid @RequestBody EncryptCardRequestDto request) {
        return ResponseEntity.ok(rsaEncryptionService.encryptCard(request));
    }
}
