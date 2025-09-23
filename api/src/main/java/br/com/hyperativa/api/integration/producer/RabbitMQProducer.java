package br.com.hyperativa.api.integration.producer;

import br.com.hyperativa.api.integration.dto.CardMessageDto;
import br.com.hyperativa.api.service.IMessageProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQProducer implements IMessageProducer {

    @Value("${hyperativa.rabbitmq.exchange}")
    private String exchange;

    @Value("${hyperativa.rabbitmq.routingkey}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // Jackson para conversão JSON

    public RabbitMQProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows // Lida com a exceção checada de writeValueAsString
    public void sendMessage(CardMessageDto message) {
        String jsonMessage = objectMapper.writeValueAsString(message);
        log.info("Sending message to RabbitMQ: {}", jsonMessage);
        rabbitTemplate.convertAndSend(exchange, routingKey, jsonMessage);
    }
}