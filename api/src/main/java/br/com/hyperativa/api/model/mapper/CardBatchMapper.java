package br.com.hyperativa.api.model.mapper;

import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.api.model.entity.CardBatch;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardBatchMapper {

    public static BatchStatusResponseDto toBatchStatusResponseDto(CardBatch entity) {
        if (entity == null) {
            return null;
        }

        BatchStatusResponseDto dto = new BatchStatusResponseDto();
        dto.setJobId(entity.getId());
        dto.setStatus(entity.getStatus().name());
        return dto;
    }
}