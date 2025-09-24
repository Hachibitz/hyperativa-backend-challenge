package br.com.hyperativa.api.repository;

import br.com.hyperativa.api.model.entity.CardBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardBatchRepository extends JpaRepository<CardBatch, UUID> {
}