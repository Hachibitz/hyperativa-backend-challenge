package br.com.hyperativa.api.integration.producer;

import br.com.hyperativa.api.integration.dto.CardMessageDto;
import br.com.hyperativa.api.integration.producer.strategy.AbstractOutboxProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CardProducer extends AbstractOutboxProducer<CardMessageDto> {

    private final String exchange;
    private final String routingKey;

    public CardProducer(
            @Value("${hyperativa.rabbitmq.exchange}") String exchange,
            @Value("${hyperativa.rabbitmq.routingkey}") String routingKey
    ) {
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void sendMessage(CardMessageDto message) {
        super.createOutboxEvent(exchange, routingKey, message);
    }
}