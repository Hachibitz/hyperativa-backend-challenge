package br.com.hyperativa.card_consumer.integration.consumer;

import br.com.hyperativa.card_common.entity.Card;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import br.com.hyperativa.card_common.repository.CardRepository;
import br.com.hyperativa.card_common.service.impl.EncryptionService;
import br.com.hyperativa.card_common.util.HashingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CardReceivedConsumer {

    private final CardRepository cardRepository;
    private final CardBatchRepository cardBatchRepository;
    private final HashingUtil hashingUtil;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    public CardReceivedConsumer(CardRepository cardRepository, CardBatchRepository cardBatchRepository, HashingUtil hashingUtil,
                                EncryptionService encryptionService, ObjectMapper objectMapper) {
        this.cardRepository = cardRepository;
        this.cardBatchRepository = cardBatchRepository;
        this.hashingUtil = hashingUtil;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${hyperativa.rabbitmq.queue}")
    @Transactional
    @SneakyThrows
    public void receiveCardMessage(String jsonMessage) {
        log.info("Received message from queue: {}", jsonMessage);

        CardMessageDto message = deserializeMessage(jsonMessage);
        String cardNumber = message.getCardNumber();
        String hash = hashingUtil.hashString(cardNumber);

        if (isCardAlreadyProcessed(hash)) {
            log.warn("Card with hash {} already exists. Acknowledging and skipping message.", hash);
            return;
        }

        CardBatch batch = findBatch(message.getBatchId());
        Card newCard = createCard(cardNumber, hash, batch);
        cardRepository.save(newCard);
        log.info("Successfully processed and saved card with ID {} and hash {}", newCard.getId(), hash);
    }

    private CardMessageDto deserializeMessage(String jsonMessage) throws Exception {
        return objectMapper.readValue(jsonMessage, CardMessageDto.class);
    }

    private boolean isCardAlreadyProcessed(String hash) {
        return cardRepository.findByCardNumberHash(hash).isPresent();
    }

    private CardBatch findBatch(java.util.UUID batchId) {
        CardBatch batch = cardBatchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            log.error("Batch with ID {} not found for card message. Card will be saved without batch link.", batchId);
        }
        return batch;
    }

    private Card createCard(String cardNumber, String hash, CardBatch batch) {
        Card newCard = new Card();
        newCard.setEncryptedCardNumber(encryptionService.encrypt(cardNumber));
        newCard.setCardNumberHash(hash);
        newCard.setCardBatch(batch);
        return newCard;
    }
}