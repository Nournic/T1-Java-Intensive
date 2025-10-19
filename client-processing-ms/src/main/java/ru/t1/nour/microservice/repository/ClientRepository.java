package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.nour.microservice.model.Client;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByDocumentTypeAndDocumentId(DocumentType documentType, String documentId);
    Optional<Client> findByDocumentId(String documentId);
    Optional<Client> findClientByUser_Id(long id);
}