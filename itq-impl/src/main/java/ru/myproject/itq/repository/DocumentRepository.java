package ru.myproject.itq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.myproject.itq.entity.Document;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    @EntityGraph(attributePaths = "documentHistories")
    Optional<Document> findWithHistoriesByDocumentId(Long documentId);

    Page<Document> findByDocumentIdIn(Iterable<Long> id, Pageable pageable);
}
