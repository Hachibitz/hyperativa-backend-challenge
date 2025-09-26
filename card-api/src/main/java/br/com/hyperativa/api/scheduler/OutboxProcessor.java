package br.com.hyperativa.api.scheduler;

import br.com.hyperativa.api.model.entity.OutboxEvent;
import br.com.hyperativa.api.model.enums.OutboxEventStatus;
import br.com.hyperativa.api.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * JOB 1: Processa novos eventos pendentes.
     * Roda com alta frequência para garantir baixa latência.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingEvents() {
        log.trace("Running job to process PENDING outbox events...");
        List<OutboxEvent> pendingEvents = outboxEventRepository.findTop100ByStatusOrderByAuditCreatedAtAsc(OutboxEventStatus.PENDING);

        if (!pendingEvents.isEmpty()) {
            log.info("Found {} PENDING outbox event(s) to process.", pendingEvents.size());
            publishEvents(pendingEvents);
        }
    }

    /**
     * JOB 2: Processa eventos que falharam ou estão presos.
     * Roda com baixa frequência e com um tempo de espera para não sobrecarregar
     * sistemas que podem estar temporariamente indisponíveis.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryStuckEvents() {
        log.trace("Running job to retry STUCK outbox events...");

        Instant cutoff = Instant.now().minus(Duration.ofMinutes(1));
        List<OutboxEventStatus> statusesToRetry = List.of(OutboxEventStatus.FAILURE, OutboxEventStatus.PENDING);
        List<OutboxEvent> stuckEvents = outboxEventRepository.findStuckEvents(statusesToRetry, cutoff);

        if (!stuckEvents.isEmpty()) {
            log.warn("Found {} STUCK outbox event(s) to retry.", stuckEvents.size());
            publishEvents(stuckEvents);
        }
    }

    /**
     * Método privado reutilizável para publicar uma lista de eventos e atualizar seu status.
     * @param events A lista de eventos a serem publicados.
     */
    private void publishEvents(List<OutboxEvent> events) {
        for (OutboxEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(event.getDestinationExchange(), event.getDestinationRoutingKey(), event.getPayload());
                event.setStatus(OutboxEventStatus.SUCCESS);
                event.setErrorDescription(null);
                log.info("Successfully sent outbox event: {}", event.getId());
            } catch (Exception e) {
                log.error("Dispatch failed for outbox event: {}. Marking as FAILURE.", event.getId(), e);
                event.setStatus(OutboxEventStatus.FAILURE);
                event.setErrorDescription(e.getMessage());
            }
        }
        outboxEventRepository.saveAll(events);
    }
}