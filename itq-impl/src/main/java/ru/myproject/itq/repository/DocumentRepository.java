package ru.myproject.itq.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.enums.DocumentStatus;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    @EntityGraph(attributePaths = "documentHistories")
    Optional<Document> findWithHistoriesByDocumentId(Long documentId);

    Page<Document> findByDocumentIdIn(Iterable<Long> id, Pageable pageable);

    @Query("select d.documentId from Document d where d.status = :status order by d.documentId asc")
    List<Long> findIdsByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    Long countByStatus(DocumentStatus status);
}
