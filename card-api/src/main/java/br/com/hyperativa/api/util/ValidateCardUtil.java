package br.com.hyperativa.api.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator; // IMPORTANTE: Usar o Validator do Jakarta
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidateCardUtil {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Valida um número de cartão de crédito usando o Algoritmo de Luhn.
     * Lança uma IllegalArgumentException se o número for inválido.
     * @param cardNumber O número do cartão em texto puro a ser validado.
     */
    public static void validateCardNumber(String cardNumber) {
        CardNumberWrapper wrapper = new CardNumberWrapper(cardNumber);

        Set<ConstraintViolation<CardNumberWrapper>> violations = validator.validate(wrapper);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Invalid card number format: {}", errors);
            throw new IllegalArgumentException("Invalid card number format: " + errors);
        }
    }

    /**
     * Classe de apoio interna (record) usada apenas para a validação programática.
     * Permite que o Validator aplique a anotação @CreditCardNumber a uma string.
     */
    private record CardNumberWrapper(@CreditCardNumber(message = "Invalid credit card number format") String cardNumber) {}
}