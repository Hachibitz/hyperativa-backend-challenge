package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.CardAlreadyExistsException;
import br.com.hyperativa.api.exception.CardNotFoundException;
import br.com.hyperativa.api.exception.CardProcessingException;
import br.com.hyperativa.api.integration.producer.CardProducer;
import br.com.hyperativa.api.model.dto.request.EncryptedCardInsertRequestDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import br.com.hyperativa.api.service.ICardService;
import br.com.hyperativa.api.util.ValidateCardUtil;
import br.com.hyperativa.card_common.entity.Card;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.enums.BatchStatusEnum;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import br.com.hyperativa.card_common.repository.CardRepository;
import br.com.hyperativa.card_common.service.impl.EncryptionService;
import br.com.hyperativa.card_common.util.HashingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class CardServiceImpl implements ICardService {

    private final CardProducer cardProducer;
    private final CardRepository cardRepository;
    private final HashingUtil hashingUtil;
    private final EncryptionService encryptionService;
    private final CardBatchRepository cardBatchRepository;
    private final RsaDecryptionService rsaDecryptionService;

    public CardServiceImpl(CardProducer cardProducer, CardRepository cardRepository, HashingUtil hashingUtil,
                           EncryptionService encryptionService, CardBatchRepository cardBatchRepository, RsaDecryptionService rsaDecryptionService) {
        this.cardProducer = cardProducer;
        this.cardRepository = cardRepository;
        this.hashingUtil = hashingUtil;
        this.encryptionService = encryptionService;
        this.cardBatchRepository = cardBatchRepository;
        this.rsaDecryptionService = rsaDecryptionService;
    }

    @Override
    @Transactional
    public UploadCardsResponseDTO processCardFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null.");
        }

        CardBatch batch = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            batch = processHeaderAndCreateBatch(reader, file.getOriginalFilename());
            streamFileLinesToQueue(reader, batch.getId());
            return new UploadCardsResponseDTO(batch.getId().toString(), "File received and processing started.");
        } catch (Exception e) {
            handleProcessingError(e, batch, file.getOriginalFilename());
            throw new CardProcessingException(String.format("Failed to process file %s: %s", file.getOriginalFilename(), e.getMessage()), e);
        }
    }

    private CardBatch processHeaderAndCreateBatch(BufferedReader reader, String filename) throws IOException {
        String header = reader.readLine();
        if (header == null) {
            throw new CardProcessingException("File is empty or header is missing.");
        }

        String lotNumber = extractLotNumberFromHeader(header);
        CardBatch batch = createBatchRecord(filename, lotNumber);

        log.info("Starting to process file {} with Lot Number {} and Job ID {}",
                filename, lotNumber, batch.getId());

        return batch;
    }

    private void streamFileLinesToQueue(BufferedReader reader, UUID jobId) {
        reader.lines()
                .filter(this::isCardLine)
                .map(this::getCardNumberFromLine)
                .filter(cardNumber -> {
                    ValidateCardUtil.validateCardNumber(cardNumber);
                    return !cardNumber.isEmpty();
                })
                .map(cardNumber -> new CardMessageDto(cardNumber, jobId))
                .forEach(cardProducer::sendMessage);
    }

    private void handleProcessingError(Exception e, CardBatch batch, String filename) {
        log.error("Error processing file {}: {}", filename, e.getMessage(), e);
        if (batch != null) {
            batch.setStatus(BatchStatusEnum.FAILED);
            cardBatchRepository.save(batch);
            log.info("Batch {} status updated to FAILED.", batch.getId());
        }
    }

    @Override
    @Transactional
    public void insertSingleCard(EncryptedCardInsertRequestDto encryptedCardInsertRequestDto) {
        rsaDecryptCardDto(encryptedCardInsertRequestDto);
        String cardNumber = encryptedCardInsertRequestDto.getCardNumber();
        log.info("Attempting to insert single card after decryption.");
        ValidateCardUtil.validateCardNumber(cardNumber);

        String hash = hashingUtil.hashString(cardNumber);
        if (cardRepository.findByCardNumberHash(hash).isPresent()) {
            log.warn("Attempted to insert a card that already exists with hash: {}", hash);
            throw new CardAlreadyExistsException("Card number already registered.");
        }

        Card newCard = new Card();
        newCard.setEncryptedCardNumber(encryptionService.encrypt(cardNumber));
        newCard.setCardNumberHash(hash);

        cardRepository.save(newCard);
        log.info("Successfully inserted single card with ID {}", newCard.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public String checkCardExists(String cardNumber) {
        String decryptedCardNumber = rsaDecryptCardNumber(cardNumber);
        log.info("Checking for existence of a card after decryption.");
        ValidateCardUtil.validateCardNumber(decryptedCardNumber);
        String hash = hashingUtil.hashString(decryptedCardNumber);

        return cardRepository.findByCardNumberHash(hash)
                .map(card -> card.getId().toString())
                .orElseThrow(() -> {
                    log.warn("Card with hash {} not found.", hash);
                    return new CardNotFoundException("Card not found.");
                });
    }

    private void rsaDecryptCardDto(EncryptedCardInsertRequestDto encryptedCardInsertRequestDto) {
        encryptedCardInsertRequestDto.setCardNumber(rsaDecryptCardNumber(encryptedCardInsertRequestDto.getCardNumber()));
    }

    private String rsaDecryptCardNumber(String cardNumber) {
        return rsaDecryptionService.decrypt(cardNumber);
    }

    private CardBatch createBatchRecord(String fileName, String lotNumber) {
        CardBatch batch = new CardBatch();
        batch.setOriginalFileName(fileName);
        batch.setLotNumber(lotNumber);
        batch.setProcessingDate(LocalDate.now());
        batch.setStatus(BatchStatusEnum.PROCESSING);
        return cardBatchRepository.save(batch);
    }

    private String extractLotNumberFromHeader(String headerLine) {
        if (headerLine.length() >= 45) {
            return headerLine.substring(37, 45).trim();
        }
        log.warn("Header line is too short to extract lot number. Defaulting to 'UNKNOWN'.");
        return "UNKNOWN";
    }

    private boolean isCardLine(String line) {
        return line.length() > 8 && line.matches("^C\\d+\\s+\\d+.*");
    }

    private String getCardNumberFromLine(String line) {
        // Remove múltiplos espaços e pega o segundo "token" da linha
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 2) {
            return parts[1]; // Retorna a segunda parte (número do cartão)
        }
        return "";
    }
}