package br.com.hyperativa.api.model.entity;

import br.com.hyperativa.api.model.enums.BatchStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "card_batches")
public class CardBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String lotNumber;

    @Column(nullable = false)
    private LocalDate processingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatusEnum status;

    @Embedded
    private Audit audit;
}
