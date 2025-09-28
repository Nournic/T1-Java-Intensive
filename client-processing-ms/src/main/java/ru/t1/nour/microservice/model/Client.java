package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "clients")
public class Client extends AbstractPersistable<Long> {
    /**
     * Format: XXFFNNNNNNNN,
     * where. XX - region number,
     * FF - bank division number,
     * NNNNNNNN - ordinal number
     * */
    @Column(name = "client_id", unique = true, nullable = false, updatable = false)
    private String clientId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false, updatable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "document_prefix")
    private String documentPrefix;

    @Column(name = "document_suffix")
    private String documentSuffix;
}
