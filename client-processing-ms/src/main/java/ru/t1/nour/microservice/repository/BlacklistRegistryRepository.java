package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.t1.nour.microservice.model.BlacklistRegistry;
import ru.t1.nour.microservice.model.enums.DocumentType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BlacklistRegistryRepository extends JpaRepository<BlacklistRegistry, Long> {
    /**
     * Проверяет, есть ли активная запись в черном списке для данной пары (тип документа, номер документа).
     * Запись считается активной, если у нее нет даты окончания срока действия ИЛИ эта дата еще не наступила.
     * @param documentType Тип документа
     * @param documentId Номер документа
     * @param currentDate Текущая дата для сравнения
     * @return true, если запись найдена и активна, иначе false.
     */
    @Query("SELECT COUNT(b) > 0 FROM BlacklistRegistry b " +
            "WHERE b.documentType = :documentType AND b.documentId = :documentId " +
            "AND (b.expirationDate IS NULL OR b.expirationDate >= :currentDate)")
    boolean isCurrentlyBlacklisted(DocumentType documentType, String documentId, LocalDateTime currentDate);
}