package br.com.hyperativa.api.model.mapper;

import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.enums.BatchStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardBatchMapperTest {

    @Test
    @DisplayName("Deve mapear uma entidade CardBatch válida para um DTO com sucesso")
    void toBatchStatusResponseDto_WithValidEntity_ShouldMapCorrectly() {
        CardBatch entity = new CardBatch();
        UUID jobId = UUID.randomUUID();
        entity.setId(jobId);
        entity.setStatus(BatchStatusEnum.PROCESSING);

        BatchStatusResponseDto dto = CardBatchMapper.toBatchStatusResponseDto(entity);

        assertNotNull(dto);
        assertEquals(jobId, dto.getJobId());
        assertEquals(BatchStatusEnum.PROCESSING.name(), dto.getStatus());
    }

    @Test
    @DisplayName("Deve retornar nulo quando a entidade de entrada for nula")
    void toBatchStatusResponseDto_WithNullEntity_ShouldReturnNull() {
        BatchStatusResponseDto dto = CardBatchMapper.toBatchStatusResponseDto(null);

        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear o JobId para nulo se o ID da entidade for nulo")
    void toBatchStatusResponseDto_WithNullIdInEntity_ShouldMapJobIdAsNull() {
        CardBatch entity = new CardBatch();
        entity.setId(null);
        entity.setStatus(BatchStatusEnum.COMPLETED);

        BatchStatusResponseDto dto = CardBatchMapper.toBatchStatusResponseDto(entity);

        assertNotNull(dto);
        assertNull(dto.getJobId());
        assertEquals(BatchStatusEnum.COMPLETED.name(), dto.getStatus());
    }

    @Test
    @DisplayName("Deve lançar NullPointerException se o status da entidade for nulo")
    void toBatchStatusResponseDto_WithNullStatusInEntity_ShouldThrowNullPointerException() {
        CardBatch entity = new CardBatch();
        entity.setId(UUID.randomUUID());
        entity.setStatus(null);

        assertThrows(NullPointerException.class, () -> {
            CardBatchMapper.toBatchStatusResponseDto(entity);
        });
    }
}