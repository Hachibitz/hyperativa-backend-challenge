package br.com.hyperativa.card_common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_number_hash", columnList = "cardNumberHash", unique = true)
})
public class Card {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @Column(nullable = false)
    private String encryptedCardNumber;

    @Column(nullable = false, unique = true)
    private String cardNumberHash;

    @Embedded
    private Audit audit = new Audit();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_batch_id")
    private CardBatch cardBatch;
}