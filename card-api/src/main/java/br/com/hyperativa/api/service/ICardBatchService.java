package br.com.hyperativa.api.service;

import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;

import java.util.UUID;

public interface ICardBatchService {
    BatchStatusResponseDto getBatchStatus(UUID jobId);
    void retryFailedBatch(UUID jobId);
}
