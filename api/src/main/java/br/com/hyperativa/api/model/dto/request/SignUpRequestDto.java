package br.com.hyperativa.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequestDto {
    @NotBlank
    @Size(min = 4)
    private String username;
    @NotBlank @Size(min = 8)
    private String password;
}
