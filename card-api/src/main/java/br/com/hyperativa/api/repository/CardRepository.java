package br.com.hyperativa.api.repository;

import br.com.hyperativa.api.model.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardNumberHash(String hash);
}