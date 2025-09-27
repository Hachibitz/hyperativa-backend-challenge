package br.com.hyperativa.api.service.impl;

import br.com.hyperativa.api.exception.CardAlreadyExistsException;
import br.com.hyperativa.api.exception.CardNotFoundException;
import br.com.hyperativa.api.integration.producer.CardProducer;
import br.com.hyperativa.api.model.dto.request.EncryptedCardInsertRequestDto;
import br.com.hyperativa.api.model.dto.response.UploadCardsResponseDTO;
import br.com.hyperativa.api.util.ValidateCardUtil;
import br.com.hyperativa.card_common.entity.Card;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.enums.BatchStatusEnum;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import br.com.hyperativa.card_common.repository.CardRepository;
import br.com.hyperativa.card_common.service.impl.EncryptionService;
import br.com.hyperativa.card_common.util.HashingUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardProducer cardProducer;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private HashingUtil hashingUtil;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private CardBatchRepository cardBatchRepository;
    @Mock
    private RsaDecryptionService rsaDecryptionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private final String VALID_CARD = "49927398716";
    private final String INVALID_CARD = "123456789";

    private void setupBatchRepositoryMock() {
        when(cardBatchRepository.save(any(CardBatch.class))).thenAnswer(invocation -> {
            CardBatch batchToSave = invocation.getArgument(0);
            if (batchToSave.getId() == null) {
                batchToSave.setId(UUID.randomUUID());
            }
            return batchToSave;
        });
    }

    @Nested
    @DisplayName("Testes para processCardFile")
    class ProcessCardFileTests {

        @Test
        @DisplayName("Deve processar arquivo com sucesso e finalizar lote como COMPLETED")
        void processCardFile_WithValidFile_ShouldSucceedAndCompleteBatch() throws IOException {
            String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000001\n" +
                    "C1     " + VALID_CARD;
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes(StandardCharsets.UTF_8));
            ArgumentCaptor<CardBatch> batchCaptor = ArgumentCaptor.forClass(CardBatch.class);

            setupBatchRepositoryMock();

            try (MockedStatic<ValidateCardUtil> mockedValidator = mockStatic(ValidateCardUtil.class)) {
                mockedValidator.when(() -> ValidateCardUtil.validateCardNumber(VALID_CARD)).then(invocation -> null);

                UploadCardsResponseDTO response = cardService.processCardFile(file, true);

                assertNotNull(response);
                assertNotNull(response.getJobId());
                verify(cardProducer, times(1)).sendMessage(any(CardMessageDto.class));
                verify(cardBatchRepository, times(2)).save(batchCaptor.capture());

                CardBatch finalBatch = batchCaptor.getValue();
                assertEquals(BatchStatusEnum.COMPLETED, finalBatch.getStatus());
            }
        }

        @Test
        @DisplayName("Deve processar arquivo com cartões inválidos e finalizar lote como COMPLETED_WITH_ERRORS")
        void processCardFile_WithInvalidCards_ShouldCompleteWithErrors() {
            String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000001\n" +
                    "C1     " + INVALID_CARD;
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes(StandardCharsets.UTF_8));
            ArgumentCaptor<CardBatch> batchCaptor = ArgumentCaptor.forClass(CardBatch.class);

            setupBatchRepositoryMock();

            try (MockedStatic<ValidateCardUtil> mockedValidator = mockStatic(ValidateCardUtil.class)) {
                mockedValidator.when(() -> ValidateCardUtil.validateCardNumber(INVALID_CARD)).thenThrow(new IllegalArgumentException());

                cardService.processCardFile(file, true);

                verify(cardProducer, never()).sendMessage(any(CardMessageDto.class));
                verify(cardBatchRepository, times(2)).save(batchCaptor.capture());

                CardBatch finalBatch = batchCaptor.getValue();
                assertEquals(BatchStatusEnum.COMPLETED_WITH_ERRORS, finalBatch.getStatus());
            }
        }

        @Test
        @DisplayName("Deve processar todos os cartões sem validar Luhn quando isToUseLuhnAlg for false")
        void processCardFile_WhenLuhnIsDisabled_ShouldProcessAllCards() {
            String fileContent = "DESAFIO-HYPERATIVA           20180524LOTE0001000001\n" +
                    "C1     " + INVALID_CARD;
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes(StandardCharsets.UTF_8));

            setupBatchRepositoryMock();

            try (MockedStatic<ValidateCardUtil> mockedValidator = mockStatic(ValidateCardUtil.class)) {
                cardService.processCardFile(file, false);

                mockedValidator.verify(() -> ValidateCardUtil.validateCardNumber(anyString()), never());
                verify(cardProducer, times(1)).sendMessage(any(CardMessageDto.class));
            }
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para arquivo nulo ou vazio")
        void processCardFile_WithEmptyFile_ShouldThrowException() {
            MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

            assertThrows(IllegalArgumentException.class, () -> cardService.processCardFile(emptyFile, true));
            assertThrows(IllegalArgumentException.class, () -> cardService.processCardFile(null, true));
        }
    }

    @Nested
    @DisplayName("Testes para insertSingleCard")
    class InsertSingleCardTests {

        @Test
        @DisplayName("Deve inserir um novo cartão com sucesso")
        void insertSingleCard_WithNewCard_ShouldSucceed() {
            EncryptedCardInsertRequestDto requestDto = new EncryptedCardInsertRequestDto();
            requestDto.setCardNumber("encryptedCard");

            when(rsaDecryptionService.decrypt("encryptedCard")).thenReturn(VALID_CARD);
            when(hashingUtil.hashString(VALID_CARD)).thenReturn("hashedCard");
            when(cardRepository.findByCardNumberHash("hashedCard")).thenReturn(Optional.empty());

            try (MockedStatic<ValidateCardUtil> mockedValidator = mockStatic(ValidateCardUtil.class)) {
                cardService.insertSingleCard(requestDto, true);

                mockedValidator.verify(() -> ValidateCardUtil.validateCardNumber(VALID_CARD));
                verify(cardRepository).save(any(Card.class));
            }
        }

        @Test
        @DisplayName("Deve lançar CardAlreadyExistsException se o cartão já existir")
        void insertSingleCard_WhenCardExists_ShouldThrowException() {
            EncryptedCardInsertRequestDto requestDto = new EncryptedCardInsertRequestDto();
            requestDto.setCardNumber("encryptedCard");

            when(rsaDecryptionService.decrypt("encryptedCard")).thenReturn(VALID_CARD);
            when(hashingUtil.hashString(VALID_CARD)).thenReturn("hashedCard");
            when(cardRepository.findByCardNumberHash("hashedCard")).thenReturn(Optional.of(new Card()));

            assertThrows(CardAlreadyExistsException.class, () -> cardService.insertSingleCard(requestDto, true));
        }
    }

    @Nested
    @DisplayName("Testes para checkCardExists")
    class CheckCardExistsTests {

        @Test
        @DisplayName("Deve retornar o ID do cartão se ele existir")
        void checkCardExists_WhenCardExists_ShouldReturnCardId() {
            String encryptedCard = "encryptedCard";
            String decryptedCard = "decryptedCardNumber";
            String hashedCard = "hashedCard";
            UUID cardId = UUID.randomUUID();
            Card existingCard = new Card();
            existingCard.setId(cardId);

            when(rsaDecryptionService.decrypt(encryptedCard)).thenReturn(decryptedCard);
            when(hashingUtil.hashString(decryptedCard)).thenReturn(hashedCard);
            when(cardRepository.findByCardNumberHash(hashedCard)).thenReturn(Optional.of(existingCard));

            String result = cardService.checkCardExists(encryptedCard);

            assertEquals(cardId.toString(), result);
        }

        @Test
        @DisplayName("Deve lançar CardNotFoundException se o cartão não existir")
        void checkCardExists_WhenCardDoesNotExist_ShouldThrowException() {
            String encryptedCard = "encryptedCard";
            String decryptedCard = "decryptedCardNumber";
            String hashedCard = "hashedCard";

            when(rsaDecryptionService.decrypt(encryptedCard)).thenReturn(decryptedCard);
            when(hashingUtil.hashString(decryptedCard)).thenReturn(hashedCard);
            when(cardRepository.findByCardNumberHash(hashedCard)).thenReturn(Optional.empty());

            assertThrows(CardNotFoundException.class, () -> cardService.checkCardExists(encryptedCard));
        }
    }
}