package br.com.hyperativa.api.service;

import br.com.hyperativa.api.integration.dto.CardMessageDto;

/**
 * Serviço responsável por criar eventos na tabela Outbox.
 * A criação do evento e a operação de negócio devem ocorrer na mesma transação.
 */
public interface IOutboxService {
    void createAndPublishCardEvent(CardMessageDto message);
}