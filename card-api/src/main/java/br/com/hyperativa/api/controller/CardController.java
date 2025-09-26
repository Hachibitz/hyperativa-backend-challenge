package br.com.hyperativa.api.controller;

import br.com.hyperativa.api.aop.Loggable;
import br.com.hyperativa.api.model.dto.request.EncryptedCardInsertRequestDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import br.com.hyperativa.api.service.ICardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/cards")
@Tag(name = "Cards", description = "Endpoints para gerenciamento de cartões")
public class CardController {

    private final ICardService cardService;

    public CardController(ICardService cardService) {
        this.cardService = cardService;
    }

    @Operation(summary = "Processa um arquivo TXT de cartões", description = "Recebe um arquivo TXT no formato especificado, cria um lote de processamento e enfileira os cartões para registro assíncrono. Retorna um ID de job para acompanhamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Arquivo recebido e processamento iniciado"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou vazio", content = @Content)
    })
    @PostMapping("/upload")
    @Loggable
    public ResponseEntity<UploadCardsResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        UploadCardsResponseDTO response = cardService.processCardFile(file);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Insere um único cartão (com E2EE)", description = "Registra um novo número de cartão de forma síncrona. O corpo da requisição deve conter o número do cartão criptografado com a chave pública da API (obtida em /security/public-key).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cartão inserido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou falha na decriptografia", content = @Content),
            @ApiResponse(responseCode = "409", description = "O cartão informado já existe na base", content = @Content)
    })
    @PostMapping
    @Loggable
    public ResponseEntity<Void> insertSingleCard(@Valid @RequestBody EncryptedCardInsertRequestDto encryptedCardInsertRequestDto) {
        cardService.insertSingleCard(encryptedCardInsertRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Verifica a existência de um cartão (com E2EE)", description = "Consulta se um determinado número de cartão existe na base. O número do cartão no body deve ser enviado criptografado com a chave pública da API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cartão encontrado, retorna o ID único do sistema"),
            @ApiResponse(responseCode = "404", description = "Cartão não encontrado", content = @Content)
    })
    @GetMapping("/check")
    @Loggable
    public ResponseEntity<String> checkCard(@RequestParam String encryptedCard) {
        String systemId = cardService.checkCardExists(encryptedCard);
        return ResponseEntity.ok(systemId);
    }
}