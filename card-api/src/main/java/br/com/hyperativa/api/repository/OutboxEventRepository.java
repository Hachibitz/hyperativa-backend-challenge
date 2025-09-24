package br.com.hyperativa.api.repository;

import br.com.hyperativa.api.model.entity.OutboxEvent;
import br.com.hyperativa.api.model.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Busca os primeiros N eventos com status PENDING para processamento imediato.
     * O nome do método agora reflete a propriedade aninhada "audit.createdAt".
     */
    List<OutboxEvent> findTop100ByStatusOrderByAuditCreatedAtAsc(OutboxEventStatus status);

    /**
     * Busca eventos que estão "presos" (seja em PENDING ou FAILURE) há mais de um certo tempo.
     * A query JPQL foi corrigida para usar "e.audit.updatedAt".
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status IN :statuses AND e.audit.updatedAt < :cutoffTime")
    List<OutboxEvent> findStuckEvents(
            @Param("statuses") List<OutboxEventStatus> statuses,
            @Param("cutoffTime") Instant cutoffTime
    );
}

