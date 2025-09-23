package br.com.hyperativa.api.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardMessageDto {
    private String cardNumber;
    private UUID batchId;
}
