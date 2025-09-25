package br.com.hyperativa.card_common.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Classe "embutível" para campos de auditoria (createdAt, updatedAt).
 * Pode ser reutilizada em múltiplas entidades.
 */
@Data
@Embeddable
public class Audit {

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
