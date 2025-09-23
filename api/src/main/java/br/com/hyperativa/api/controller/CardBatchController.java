package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.model.dto.response.BatchStatusResponseDto;
import br.com.hyperativa.api.service.ICardBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/batches")
public class CardBatchController {

    private final ICardBatchService cardBatchService;

    public CardBatchController(ICardBatchService cardBatchService) {
        this.cardBatchService = cardBatchService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<BatchStatusResponseDto> getBatchStatus(@PathVariable UUID jobId) {
        BatchStatusResponseDto status = cardBatchService.getBatchStatus(jobId);
        return ResponseEntity.ok(status);
    }
}
