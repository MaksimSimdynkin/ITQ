package ru.myproject.itq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.myproject.itq.entity.ApprovalRegistry;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.enums.DocumentHistoryAction;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.exeption.RegistryWriteFailedRuntimeException;
import ru.myproject.itq.repository.ApprovalRegistryRepository;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.repository.HistoryRepository;
import ru.myproject.itq.service.impl.DocumentWorkService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest(properties = {
        "app.workers.submit.enabled=false",
        "app.workers.approve.enabled=false"
})
class ApprovalRollbackIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("soft")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.liquibase.url", postgres::getJdbcUrl);
        r.add("spring.liquibase.user", postgres::getUsername);
        r.add("spring.liquibase.password", postgres::getPassword);
        r.add("spring.liquibase.change-log", () -> "classpath:/changelog/db.changelog-master.yaml");
    }

    @Autowired
    DocumentRepository documentRepository;
    @Autowired
    ApprovalRegistryRepository approvalRegistryRepository;
    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    DocumentWorkService documentWorkService;

    @Test
    void approve_shouldRollback_whenRegistryInsertFails() {
        Document doc = Document.builder()
                .author("Ivan")
                .title("Doc")
                .status(DocumentStatus.DRAFT)
                .build();

        doc = documentRepository.saveAndFlush(doc);
        Long docId = doc.getDocumentId();
        doc = documentRepository.findById(docId).orElseThrow();
        assertNotNull(doc.getNumber());

        var submitRes = documentWorkService.submitOne(docId, "tester", "submit");
        assertEquals(ru.myproject.itq.enums.BatchItemResult.SUCCESS, submitRes);

        approvalRegistryRepository.saveAndFlush(ApprovalRegistry.builder()
                .documentId(doc)
                .documentNumber(doc.getNumber())
                .approvedBy("someone")
                .build());

        assertEquals(1, approvalRegistryRepository.countByDocument_DocumentId(docId));

        assertThrows(RegistryWriteFailedRuntimeException.class, () ->
                documentWorkService.approveOne(docId, "approver", "ok")
        );

        Document after = documentRepository.findById(docId).orElseThrow();
        assertEquals(DocumentStatus.SUBMITTED, after.getStatus(), "Approve must rollback status change");

        long submitCount = historyRepository.countByDocument_DocumentIdAndAction(docId, DocumentHistoryAction.SUBMIT);
        long approveCount = historyRepository.countByDocument_DocumentIdAndAction(docId, DocumentHistoryAction.APPROVE);

        assertEquals(1, submitCount);
        assertEquals(0, approveCount, "Approve history must rollback");

        assertEquals(1, approvalRegistryRepository.countByDocument_DocumentId(docId));
    }
}
