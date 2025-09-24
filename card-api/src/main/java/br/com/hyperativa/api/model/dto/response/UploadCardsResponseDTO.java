package br.com.hyperativa.api.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadCardsResponseDTO {
    private String jobId;
    private String message;
}
