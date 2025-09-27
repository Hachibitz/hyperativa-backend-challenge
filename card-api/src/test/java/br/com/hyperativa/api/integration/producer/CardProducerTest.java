package br.com.hyperativa.api.integration.producer;

import br.com.hyperativa.api.model.entity.OutboxEvent;
import br.com.hyperativa.api.repository.OutboxEventRepository;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardProducerTest {

    private static final String TEST_EXCHANGE = "cards.exchange.test";
    private static final String TEST_ROUTING_KEY = "cards.routingkey.test";

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private CardProducer cardProducer;

    @BeforeEach
    void setUp() {
        cardProducer = new CardProducer(TEST_EXCHANGE, TEST_ROUTING_KEY);
        ReflectionTestUtils.setField(cardProducer, "outboxEventRepository", outboxEventRepository);
        ReflectionTestUtils.setField(cardProducer, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("Deve criar um OutboxEvent com os dados corretos ao chamar sendMessage")
    void sendMessage_ShouldCreateOutboxEventWithCorrectData() throws Exception {
        UUID jobId = UUID.randomUUID();
        CardMessageDto message = new CardMessageDto("4242424242424242", jobId);
        String expectedPayload = "{\"cardNumber\":\"4242424242424242\",\"batchId\":\"" + jobId + "\"}";

        when(objectMapper.writeValueAsString(message)).thenReturn(expectedPayload);
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);

        cardProducer.sendMessage(message);

        verify(outboxEventRepository).save(eventCaptor.capture());
        OutboxEvent capturedEvent = eventCaptor.getValue();

        assertEquals(TEST_EXCHANGE, capturedEvent.getDestinationExchange());
        assertEquals(TEST_ROUTING_KEY, capturedEvent.getDestinationRoutingKey());
        assertEquals(expectedPayload, capturedEvent.getPayload());
    }
}