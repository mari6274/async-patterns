package dev.maczkowski.jugpoznan.asyncpatterns.application;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "APPLICATION")
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEntity {

    @Id
    @GeneratedValue
    private Long id;
    String uuid;
    String firstName;
    String lastName;
    @Enumerated(EnumType.STRING)
    Profession profession;
    Long incomeAmount;

    @PrePersist
    void generateUuid() {
        this.uuid = UUID.randomUUID().toString();
    }
}
