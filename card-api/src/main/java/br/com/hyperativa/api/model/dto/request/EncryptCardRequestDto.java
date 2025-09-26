package br.com.hyperativa.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EncryptCardRequestDto {
    @NotBlank
    private String cardNumber;
    @NotBlank
    private String publicKey;
}