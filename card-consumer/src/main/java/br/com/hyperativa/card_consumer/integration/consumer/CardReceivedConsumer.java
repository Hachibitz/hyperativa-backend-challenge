package br.com.hyperativa.card_consumer.integration.consumer;

import br.com.hyperativa.card_common.util.MaskingUtil;
import br.com.hyperativa.card_consumer.exception.CardProcessingException;
import br.com.hyperativa.card_consumer.service.processor.CardReceivedProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardReceivedConsumer {

    private final CardReceivedProcessor cardProcessor;

    @RabbitListener(queues = "${hyperativa.rabbitmq.queue}", containerFactory = "retryContainerFactory")
    public void receiveCardMessage(String jsonMessage) throws JsonProcessingException {
        log.info("Received message from queue: {}", MaskingUtil.maskJsonString(jsonMessage));
        try {
            cardProcessor.process(jsonMessage);
        } catch (Exception e) {
            log.error("Error processing message. It will be retried or sent to DLQ. Error: {}", e.getMessage());
            throw new CardProcessingException("Error processing message. It will be retried or sent to DLQ.");
        }
    }
}
