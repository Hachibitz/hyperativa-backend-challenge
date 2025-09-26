package br.com.hyperativa.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EncryptCardResponseDto {
    private String encryptedCardNumber;
}