package br.com.hyperativa.api.service;

import br.com.hyperativa.api.integration.dto.CardMessageDto;

public interface IMessageProducer {
    void sendMessage(CardMessageDto message);
}
