package ru.myproject.itq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.myproject.itq.entity.DocumentHistory;
import ru.myproject.itq.enums.DocumentHistoryAction;

public interface HistoryRepository extends JpaRepository <DocumentHistory, Long> {
    long countByDocument_DocumentIdAndAction(Long documentId, DocumentHistoryAction action);
}
