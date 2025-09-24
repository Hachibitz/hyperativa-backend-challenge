package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.api.service.ICardBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/batches")
@Tag(name = "Batches", description = "Endpoints para consulta de lotes de processamento de cartões")
public class CardBatchController {

    private final ICardBatchService cardBatchService;

    public CardBatchController(ICardBatchService cardBatchService) {
        this.cardBatchService = cardBatchService;
    }

    @Operation(summary = "Consulta o status de um lote", description = "Retorna o status atual de um lote de processamento de cartões a partir do seu ID de job.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do lote retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado para o ID informado", content = @Content)
    })
    @GetMapping("/{jobId}")
    @Loggable
    public ResponseEntity<BatchStatusResponseDto> getBatchStatus(@PathVariable UUID jobId) {
        BatchStatusResponseDto status = cardBatchService.getBatchStatus(jobId);
        return ResponseEntity.ok(status);
    }
}
