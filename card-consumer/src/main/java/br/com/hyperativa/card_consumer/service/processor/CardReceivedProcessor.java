package br.com.hyperativa.card_consumer.service.processor;

import br.com.hyperativa.card_common.entity.Card;
import br.com.hyperativa.card_common.entity.CardBatch;
import br.com.hyperativa.card_common.integration.dto.CardMessageDto;
import br.com.hyperativa.card_common.repository.CardBatchRepository;
import br.com.hyperativa.card_common.repository.CardRepository;
import br.com.hyperativa.card_common.service.impl.EncryptionService;
import br.com.hyperativa.card_common.util.HashingUtil;
import br.com.hyperativa.card_common.util.MaskingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardReceivedProcessor {

    private final CardRepository cardRepository;
    private final CardBatchRepository cardBatchRepository;
    private final HashingUtil hashingUtil;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(String jsonMessage) throws JsonProcessingException {
        log.info("Processing message: {}", MaskingUtil.maskJsonString(jsonMessage));

        CardMessageDto message = deserializeMessage(jsonMessage);
        String cardNumber = message.getCardNumber();
        String hash = hashingUtil.hashString(cardNumber);

        if (isCardAlreadyProcessed(hash)) {
            log.warn("Card with hash {} already exists. Skipping message.", hash);
            return;
        }

        CardBatch batch = findBatch(message.getBatchId());
        Card newCard = createCard(cardNumber, hash, batch);

        cardRepository.save(newCard);
        log.info("Successfully processed and saved card with ID {} and hash {}", newCard.getId(), hash);
    }

    private CardMessageDto deserializeMessage(String jsonMessage) throws JsonProcessingException {
        return objectMapper.readValue(jsonMessage, CardMessageDto.class);
    }

    private boolean isCardAlreadyProcessed(String hash) {
        return cardRepository.findByCardNumberHash(hash).isPresent();
    }

    private CardBatch findBatch(java.util.UUID batchId) {
        return cardBatchRepository.findById(batchId).orElse(null);
    }

    private Card createCard(String cardNumber, String hash, CardBatch batch) {
        Card newCard = new Card();
        newCard.setEncryptedCardNumber(encryptionService.encrypt(cardNumber));
        newCard.setCardNumberHash(hash);
        newCard.setCardBatch(batch);
        return newCard;
    }
}
