package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.CardDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import br.com.hyperativa.api.service.ICardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final ICardService cardService;

    public CardController(ICardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/upload")
    @Loggable
    public ResponseEntity<UploadCardsResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        UploadCardsResponseDTO response = cardService.processCardFile(file);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping
    @Loggable
    public ResponseEntity<Void> insertSingleCard(@Valid @RequestBody CardDto cardDto) {
        cardService.insertSingleCard(cardDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/check/{cardNumber}")
    @Loggable
    public ResponseEntity<String> checkCard(@PathVariable String cardNumber) {
        String systemId = cardService.checkCardExists(cardNumber);
        return ResponseEntity.ok(systemId);
    }
}