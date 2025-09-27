package br.com.hyperativa.card_consumer.integration.consumer;

import br.com.hyperativa.card_common.util.MaskingUtil;
import br.com.hyperativa.card_consumer.exception.CardProcessingException;
import br.com.hyperativa.card_consumer.service.processor.CardReceivedProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardReceivedConsumerTest {

    @Mock
    private CardReceivedProcessor cardProcessor;

    @InjectMocks
    private CardReceivedConsumer cardReceivedConsumer;

    @Test
    @DisplayName("Deve chamar o processador quando uma mensagem válida for recebida")
    void receiveCardMessage_WhenProcessSucceeds_ShouldCallProcessor() throws JsonProcessingException {
        String jsonMessage = "{\"cardNumber\":\"123\"}";

        try (MockedStatic<MaskingUtil> mockedMasking = Mockito.mockStatic(MaskingUtil.class)) {
            mockedMasking.when(() -> MaskingUtil.maskJsonString(anyString())).thenReturn("masked_message");

            assertDoesNotThrow(() -> cardReceivedConsumer.receiveCardMessage(jsonMessage));

            verify(cardProcessor).process(jsonMessage);
        }
    }

    @Test
    @DisplayName("Deve lançar CardProcessingException se o processador falhar")
    void receiveCardMessage_WhenProcessFails_ShouldThrowCardProcessingException() throws JsonProcessingException {
        String jsonMessage = "{\"cardNumber\":\"123\"}";

        doThrow(new RuntimeException("DB error")).when(cardProcessor).process(jsonMessage);

        try (MockedStatic<MaskingUtil> mockedMasking = Mockito.mockStatic(MaskingUtil.class)) {
            mockedMasking.when(() -> MaskingUtil.maskJsonString(anyString())).thenReturn("masked_message");

            assertThrows(CardProcessingException.class, () -> {
                cardReceivedConsumer.receiveCardMessage(jsonMessage);
            });

            verify(cardProcessor).process(jsonMessage);
        }
    }
}