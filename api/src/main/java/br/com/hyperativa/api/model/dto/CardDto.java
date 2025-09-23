package br.com.hyperativa.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CreditCardNumber;
import lombok.Data;

@Data
public class CardDto {
    @NotBlank(message = "Card number cannot be blank")
    @CreditCardNumber(message = "Invalid credit card number format")
    private String cardNumber;
}