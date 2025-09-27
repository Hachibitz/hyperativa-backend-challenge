package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.BatchNotFoundException;
import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.enums.BatchStatusEnum;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardBatchServiceImplTest {

    @Mock
    private CardBatchRepository cardBatchRepository;

    @InjectMocks
    private CardBatchServiceImpl cardBatchService;

    @Test
    @DisplayName("Deve retornar o status do lote quando o ID do job existir")
    void getBatchStatus_WhenBatchExists_ShouldReturnStatusDto() {
        UUID jobId = UUID.randomUUID();
        CardBatch batch = new CardBatch();
        batch.setId(jobId);
        batch.setStatus(BatchStatusEnum.COMPLETED);
        batch.setOriginalFileName("test.txt");
        batch.setProcessingDate(LocalDate.now());
        batch.setLotNumber("LOTE001");

        when(cardBatchRepository.findById(jobId)).thenReturn(Optional.of(batch));

        BatchStatusResponseDto responseDto = cardBatchService.getBatchStatus(jobId);

        assertNotNull(responseDto);
        assertEquals(jobId, responseDto.getJobId());
        assertEquals(BatchStatusEnum.COMPLETED.name(), responseDto.getStatus());

        verify(cardBatchRepository).findById(jobId);
    }

    @Test
    @DisplayName("Deve lançar BatchNotFoundException quando o ID do job não existir")
    void getBatchStatus_WhenBatchNotFound_ShouldThrowBatchNotFoundException() {
        UUID nonExistentJobId = UUID.randomUUID();

        when(cardBatchRepository.findById(nonExistentJobId)).thenReturn(Optional.empty());

        BatchNotFoundException exception = assertThrows(BatchNotFoundException.class, () -> {
            cardBatchService.getBatchStatus(nonExistentJobId);
        });

        assertTrue(exception.getMessage().contains("Batch not found for ID: " + nonExistentJobId));

        verify(cardBatchRepository).findById(nonExistentJobId);
    }

    @Test
    @DisplayName("Deve lançar UnsupportedOperationException ao tentar reprocessar um lote")
    void retryFailedBatch_ShouldThrowUnsupportedOperationException() {
        UUID jobId = UUID.randomUUID();

        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            cardBatchService.retryFailedBatch(jobId);
        });

        assertEquals("Retrying failed batches is not implemented yet.", exception.getMessage());
    }
}
