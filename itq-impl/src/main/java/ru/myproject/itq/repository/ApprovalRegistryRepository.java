package ru.myproject.itq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.myproject.itq.entity.ApprovalRegistry;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {
    long countByDocument_DocumentId(Long documentId);
}
