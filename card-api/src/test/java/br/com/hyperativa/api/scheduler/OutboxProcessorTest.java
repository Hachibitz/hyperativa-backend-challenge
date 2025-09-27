package br.com.hyperativa.api.scheduler;

import br.com.hyperativa.api.model.entity.OutboxEvent;
import br.com.hyperativa.api.model.enums.OutboxEventStatus;
import br.com.hyperativa.api.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OutboxProcessor outboxProcessor;

    private OutboxEvent createTestEvent(OutboxEventStatus status) {
        OutboxEvent event = new OutboxEvent();
        event.setId(UUID.randomUUID());
        event.setStatus(status);
        event.setDestinationExchange("test.exchange");
        event.setDestinationRoutingKey("test.key");
        event.setPayload("{}");
        return event;
    }

    @Nested
    @DisplayName("Testes para processPendingEvents")
    class ProcessPendingEventsTests {

        @Test
        @DisplayName("Deve publicar eventos pendentes e atualizar status para SUCCESS")
        void processPendingEvents_WhenEventsExist_ShouldPublishAndUpdateToSuccess() {
            List<OutboxEvent> events = List.of(createTestEvent(OutboxEventStatus.PENDING));
            when(outboxEventRepository.findTop100ByStatusOrderByAuditCreatedAtAsc(OutboxEventStatus.PENDING)).thenReturn(events);
            ArgumentCaptor<List<OutboxEvent>> captor = ArgumentCaptor.forClass(List.class);

            outboxProcessor.processPendingEvents();

            verify(rabbitTemplate).convertAndSend("test.exchange", "test.key", "{}");
            verify(outboxEventRepository).saveAll(captor.capture());

            assertEquals(1, captor.getValue().size());
            assertEquals(OutboxEventStatus.SUCCESS, captor.getValue().getFirst().getStatus());
        }

        @Test
        @DisplayName("Deve marcar evento como FAILURE se a publicação falhar")
        void processPendingEvents_WhenPublishFails_ShouldMarkAsFailure() {
            List<OutboxEvent> events = List.of(createTestEvent(OutboxEventStatus.PENDING));
            when(outboxEventRepository.findTop100ByStatusOrderByAuditCreatedAtAsc(OutboxEventStatus.PENDING)).thenReturn(events);
            doThrow(new AmqpException("Broker unavailable")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString());
            ArgumentCaptor<List<OutboxEvent>> captor = ArgumentCaptor.forClass(List.class);

            outboxProcessor.processPendingEvents();

            verify(outboxEventRepository).saveAll(captor.capture());
            OutboxEvent failedEvent = captor.getValue().getFirst();

            assertEquals(OutboxEventStatus.FAILURE, failedEvent.getStatus());
            assertEquals("Broker unavailable", failedEvent.getErrorDescription());
        }

        @Test
        @DisplayName("Não deve fazer nada se não houver eventos pendentes")
        void processPendingEvents_WhenNoEvents_ShouldDoNothing() {
            when(outboxEventRepository.findTop100ByStatusOrderByAuditCreatedAtAsc(OutboxEventStatus.PENDING)).thenReturn(Collections.emptyList());

            outboxProcessor.processPendingEvents();

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
            verify(outboxEventRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Testes para retryStuckEvents")
    class RetryStuckEventsTests {

        @Test
        @DisplayName("Deve tentar republicar eventos presos e atualizar status para SUCCESS")
        void retryStuckEvents_WhenStuckEventsExist_ShouldPublishAndUpdateToSuccess() {
            List<OutboxEvent> events = List.of(createTestEvent(OutboxEventStatus.FAILURE));
            when(outboxEventRepository.findStuckEvents(anyList(), any(Instant.class))).thenReturn(events);
            ArgumentCaptor<List<OutboxEvent>> captor = ArgumentCaptor.forClass(List.class);

            outboxProcessor.retryStuckEvents();

            verify(rabbitTemplate).convertAndSend("test.exchange", "test.key", "{}");
            verify(outboxEventRepository).saveAll(captor.capture());

            OutboxEvent retriedEvent = captor.getValue().getFirst();
            assertEquals(OutboxEventStatus.SUCCESS, retriedEvent.getStatus());
            assertNull(retriedEvent.getErrorDescription());
        }

        @Test
        @DisplayName("Não deve fazer nada se não houver eventos presos")
        void retryStuckEvents_WhenNoStuckEvents_ShouldDoNothing() {
            when(outboxEventRepository.findStuckEvents(anyList(), any(Instant.class))).thenReturn(Collections.emptyList());

            outboxProcessor.retryStuckEvents();

            verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
            verify(outboxEventRepository, never()).saveAll(anyList());
        }
    }
}