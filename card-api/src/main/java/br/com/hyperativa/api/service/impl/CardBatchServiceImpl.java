package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.BatchNotFoundException;
import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.api.model.entity.CardBatch;
import br.com.hyperativa.api.model.mapper.CardBatchMapper;
import br.com.hyperativa.api.repository.CardBatchRepository;
import br.com.hyperativa.api.service.ICardBatchService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CardBatchServiceImpl implements ICardBatchService {

    private final CardBatchRepository cardBatchRepository;

    public CardBatchServiceImpl(CardBatchRepository cardBatchRepository) {
        this.cardBatchRepository = cardBatchRepository;
    }

    @Override
    public BatchStatusResponseDto getBatchStatus(UUID jobId) {
        CardBatch batch = cardBatchRepository.findById(jobId)
                .orElseThrow(() -> new BatchNotFoundException("Batch not found for ID: " + jobId));

        return CardBatchMapper.toBatchStatusResponseDto(batch);
    }

    @Override
    public void retryFailedBatch(UUID jobId) {
        throw new UnsupportedOperationException("Retrying failed batches is not implemented yet.");
    }
}
