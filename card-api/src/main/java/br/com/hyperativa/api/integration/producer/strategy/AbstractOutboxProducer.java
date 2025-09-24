package br.com.hyperativa.api.integration.producer.strategy;

import br.com.hyperativa.api.model.entity.OutboxEvent;
import br.com.hyperativa.api.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe base abstrata para produtores que implementam o padrão Transactional Outbox.
 * Encapsula a lógica de salvar o evento no banco de dados.
 *
 * @param <T> O tipo do payload da mensagem.
 */
@Slf4j
public abstract class AbstractOutboxProducer<T> {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * O método principal que os serviços de negócio devem chamar.
     * Ele salva o evento na tabela outbox dentro da mesma transação da operação de negócio.
     * O envio real da mensagem é delegado ao OutboxProcessor.
     */
    @Transactional
    @SneakyThrows
    protected void createOutboxEvent(String exchange, String routingKey, T payload) {
        String jsonPayload = objectMapper.writeValueAsString(payload);

        OutboxEvent event = new OutboxEvent();
        event.setDestinationExchange(exchange);
        event.setDestinationRoutingKey(routingKey);
        event.setPayload(jsonPayload);

        outboxEventRepository.save(event);
        log.info("Outbox event created for exchange [{}] with payload: {}", exchange, jsonPayload);
    }
}