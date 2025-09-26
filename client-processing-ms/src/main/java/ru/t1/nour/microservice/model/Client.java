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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Format: XXFFNNNNNNNN,
     * where. XX - region number,
     * FF - bank division number,
     * NNNNNNNN - ordinal number
     * */
    @GeneratedValue(generator = "client-id-generator")
    @GenericGenerator(name = "client-id-generator", strategy = "ru.t1.nour.microservice.util.generators.ClientIdGenerator")
    @Column(name = "client_id", unique = true, nullable = false, updatable = false)
    private String clientId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "document_prefix")
    private String documentPrefix;

    @Column(name = "document_suffix")
    private String documentSuffix;
}
