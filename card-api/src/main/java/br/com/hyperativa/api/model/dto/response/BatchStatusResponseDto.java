package br.com.hyperativa.api.model.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class BatchStatusResponseDto {
    private UUID jobId;
    private String status;
}
