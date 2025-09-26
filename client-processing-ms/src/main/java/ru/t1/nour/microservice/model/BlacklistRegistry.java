package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklist_registries", uniqueConstraints = {
        @UniqueConstraint(name = "unique_document_type_id", columnNames = {"document_type","document_id"})
})
public class BlacklistRegistry extends AbstractPersistable<Long> {
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "blacklisted_at")
    private LocalDateTime blacklisted_at;

    @Column(name = "reason")
    private String reason;

    @Column(name = "blacklist_expiration_date")
    private LocalDateTime blacklist_expiration_date;
}