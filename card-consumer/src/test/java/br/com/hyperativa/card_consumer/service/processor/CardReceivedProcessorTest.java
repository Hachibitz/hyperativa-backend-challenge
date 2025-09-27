package br.com.hyperativa.card_consumer.service.processor;

import br.com.hyperativa.card_common.entity.Card;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import br.com.hyperativa.card_common.repository.CardRepository;
import br.com.hyperativa.card_common.service.impl.EncryptionService;
import br.com.hyperativa.card_common.util.HashingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardReceivedProcessorTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardBatchRepository cardBatchRepository;
    @Mock
    private HashingUtil hashingUtil;
    @Mock
    private EncryptionService encryptionService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CardReceivedProcessor cardReceivedProcessor;

    @Test
    @DisplayName("Deve processar e salvar um novo cartão com sucesso")
    void process_WithNewCard_ShouldSaveCard() throws JsonProcessingException {
        UUID jobId = UUID.randomUUID();
        String cardNumber = "4242424242424242";
        String jsonMessage = "{\"cardNumber\":\"" + cardNumber + "\",\"batchId\":\"" + jobId + "\"}";
        CardMessageDto messageDto = new CardMessageDto(cardNumber, jobId);
        String hashedCard = "hashedCardNumber";
        String encryptedCard = "encryptedCardNumber";
        CardBatch batch = new CardBatch();

        when(objectMapper.readValue(jsonMessage, CardMessageDto.class)).thenReturn(messageDto);
        when(hashingUtil.hashString(cardNumber)).thenReturn(hashedCard);
        when(cardRepository.findByCardNumberHash(hashedCard)).thenReturn(Optional.empty());
        when(cardBatchRepository.findById(jobId)).thenReturn(Optional.of(batch));
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCard);
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        cardReceivedProcessor.process(jsonMessage);

        verify(cardRepository).save(cardCaptor.capture());
        Card savedCard = cardCaptor.getValue();

        assertEquals(hashedCard, savedCard.getCardNumberHash());
        assertEquals(encryptedCard, savedCard.getEncryptedCardNumber());
        assertEquals(batch, savedCard.getCardBatch());
    }

    @Test
    @DisplayName("Deve pular o processamento se o cartão já existir")
    void process_WithExistingCard_ShouldSkipSaving() throws JsonProcessingException {
        String jsonMessage = "{\"cardNumber\":\"123\",\"batchId\":\"" + UUID.randomUUID() + "\"}";
        CardMessageDto messageDto = new CardMessageDto("123", UUID.randomUUID());
        String hashedCard = "hashedCardNumber";

        when(objectMapper.readValue(jsonMessage, CardMessageDto.class)).thenReturn(messageDto);
        when(hashingUtil.hashString("123")).thenReturn(hashedCard);
        when(cardRepository.findByCardNumberHash(hashedCard)).thenReturn(Optional.of(new Card()));

        cardReceivedProcessor.process(jsonMessage);

        verify(cardRepository, never()).save(any(Card.class));
        verify(cardBatchRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Deve salvar o cartão com lote nulo se o lote não for encontrado")
    void process_WhenBatchNotFound_ShouldSaveCardWithNullBatch() throws JsonProcessingException {
        UUID jobId = UUID.randomUUID();
        String cardNumber = "123";
        String jsonMessage = "{\"cardNumber\":\"" + cardNumber + "\",\"batchId\":\"" + jobId + "\"}";
        CardMessageDto messageDto = new CardMessageDto(cardNumber, jobId);
        String hashedCard = "hashed-card-value";

        when(objectMapper.readValue(jsonMessage, CardMessageDto.class)).thenReturn(messageDto);
        when(hashingUtil.hashString(cardNumber)).thenReturn(hashedCard);
        when(cardRepository.findByCardNumberHash(hashedCard)).thenReturn(Optional.empty());
        when(cardBatchRepository.findById(jobId)).thenReturn(Optional.empty());
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        cardReceivedProcessor.process(jsonMessage);

        verify(cardRepository).save(cardCaptor.capture());
        assertNull(cardCaptor.getValue().getCardBatch());
    }

    @Test
    @DisplayName("Deve propagar JsonProcessingException se a desserialização falhar")
    void process_WithInvalidJson_ShouldThrowJsonProcessingException() throws JsonProcessingException {
        String invalidJson = "{invalid}";
        when(objectMapper.readValue(invalidJson, CardMessageDto.class)).thenThrow(JsonProcessingException.class);

        assertThrows(JsonProcessingException.class, () -> {
            cardReceivedProcessor.process(invalidJson);
        });
    }
}