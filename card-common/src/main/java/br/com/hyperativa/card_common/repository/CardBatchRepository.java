package br.com.hyperativa.card_common.repository;

import br.com.hyperativa.card_common.entity.CardBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardBatchRepository extends JpaRepository<CardBatch, UUID> {
}