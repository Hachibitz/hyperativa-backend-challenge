package br.com.hyperativa.card_common.repository;

import br.com.hyperativa.card_common.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardNumberHash(String hash);
}