package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "OUTBOX")
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEntity {

    @Id
    @GeneratedValue
    private Long id;
    @Size(max = 36)
    private String uuid;
    private Instant creationDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    private byte attempts;
    @Enumerated(EnumType.STRING)
    private Type type;
    private String body;

    public OutboxEntity(Type type, String body) {
        this.type = type;
        this.body = body;
    }

    @PrePersist
    void prepreToPersist() {
        this.uuid = UUID.randomUUID().toString();
        this.creationDate = Instant.now();
        this.status = Status.NEW;
        this.attempts = 0;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}
