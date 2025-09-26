package br.com.hyperativa.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EncryptedCardInsertRequestDto {
    @NotBlank(message = "Card number cannot be blank")
    private String cardNumber;
}