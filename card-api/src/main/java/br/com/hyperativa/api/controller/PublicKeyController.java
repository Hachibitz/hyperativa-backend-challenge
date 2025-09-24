package br.com.hyperativa.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.PublicKey;
import java.util.Base64;

@RestController
@RequestMapping("/security")
@Tag(name = "Security", description = "Endpoints relacionados à segurança da API")
public class PublicKeyController {

    private final PublicKey publicKey;

    public PublicKeyController(PublicKey publicKey) {
        this.publicKey = publicKey;
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
}
