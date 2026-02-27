package ru.myproject.itq;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.enums.BatchItemResult;
import ru.myproject.itq.enums.DocumentHistoryAction;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.repository.ApprovalRegistryRepository;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.repository.HistoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "app.workers.submit.enabled=false",
        "app.workers.approve.enabled=false"
})
class DocumentWorkflowIT {

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
    HistoryRepository historyRepository;
    @Autowired
    ApprovalRegistryRepository approvalRegistryRepository;
    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void cleanDb() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        approvalRegistryRepository.deleteAllInBatch();
        historyRepository.deleteAllInBatch();
        documentRepository.deleteAllInBatch();
    }

    @Test
    void happyPath_createSubmitApprove_shouldApproveAndWriteHistoryAndRegistry() throws Exception {
        Long id = createDocument("Ivan", "Doc", "creator");

        mockMvc.perform(post("/api/v1/documents/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson(List.of(id), "tester", "submit")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(id))
                .andExpect(jsonPath("$.result[0].result").value(BatchItemResult.SUCCESS.name()));

        mockMvc.perform(post("/api/v1/documents/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson(List.of(id), "approver", "approve")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(id))
                .andExpect(jsonPath("$.result[0].result").value(BatchItemResult.SUCCESS.name()));

        Document actual = documentRepository.findById(id).orElseThrow();
        assertEquals(DocumentStatus.APPROVED, actual.getStatus());

        long submitCount = historyRepository.countByDocument_DocumentIdAndAction(id, DocumentHistoryAction.SUBMIT);
        long approveCount = historyRepository.countByDocument_DocumentIdAndAction(id, DocumentHistoryAction.APPROVE);
        assertEquals(1, submitCount);
        assertEquals(1, approveCount);

        assertEquals(1, approvalRegistryRepository.countByDocument_DocumentId(id));
    }

    @Test
    void batchSubmit_shouldReturnSuccessAndMoveAllToSubmitted() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ids.add(createDocument("author-" + i, "title-" + i, "creator-" + i));
        }

        var submitResult = mockMvc.perform(post("/api/v1/documents/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson(ids, "batch-user", "batch submit")))
                .andExpect(status().isOk());

        for (int i = 0; i < ids.size(); i++) {
            submitResult.andExpect(jsonPath("$.result[" + i + "].id").value(ids.get(i)))
                    .andExpect(jsonPath("$.result[" + i + "].result").value(BatchItemResult.SUCCESS.name()));
        }

        List<Document> actual = documentRepository.findAllById(ids);
        assertEquals(5, actual.size());
        assertTrue(actual.stream().allMatch(d -> d.getStatus() == DocumentStatus.SUBMITTED));
    }

    @Test
    void batchApprove_shouldReturnPartialResults_andWriteRegistryOnlyForSuccess() throws Exception {
        Long submittedId = createDocument("submitted-author", "submitted-title", "creator");
        mockMvc.perform(post("/api/v1/documents/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson(List.of(submittedId), "prep", "submit")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(submittedId))
                .andExpect(jsonPath("$.result[0].result").value(BatchItemResult.SUCCESS.name()));

        Long draftId = createDocument("draft-author", "draft-title", "creator");
        Long missingId = 999_999_999L;

        mockMvc.perform(post("/api/v1/documents/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson(List.of(submittedId, draftId, missingId), "approver", "approve batch")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(submittedId))
                .andExpect(jsonPath("$.result[0].result").value(BatchItemResult.SUCCESS.name()))
                .andExpect(jsonPath("$.result[1].id").value(draftId))
                .andExpect(jsonPath("$.result[1].result").value(BatchItemResult.CONFLICT.name()))
                .andExpect(jsonPath("$.result[2].id").value(missingId))
                .andExpect(jsonPath("$.result[2].result").value(BatchItemResult.NOT_FOUND.name()));

        Document submittedActual = documentRepository.findById(submittedId).orElseThrow();
        Document draftActual = documentRepository.findById(draftId).orElseThrow();
        assertEquals(DocumentStatus.APPROVED, submittedActual.getStatus());
        assertEquals(DocumentStatus.DRAFT, draftActual.getStatus());

        assertEquals(1, approvalRegistryRepository.countByDocument_DocumentId(submittedId));
        assertEquals(0, approvalRegistryRepository.countByDocument_DocumentId(draftId));
        assertEquals(1, approvalRegistryRepository.count());
    }

    private Long createDocument(String author, String title, String initiator) throws Exception {
        String responseBody = mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequestJson(author, title, initiator)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractLongField(responseBody, "documentId");
    }

    private String createRequestJson(String author, String title, String initiator) {
        return String.format("{\"author\":\"%s\",\"title\":\"%s\",\"initiator\":\"%s\"}", author, title, initiator);
    }

    private String batchRequestJson(List<Long> ids, String initiator, String comment) {
        String idsJson = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return String.format("{\"ids\":[%s],\"initiator\":\"%s\",\"comment\":\"%s\"}", idsJson, initiator, comment);
    }

    private Long extractLongField(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\":(\\d+)").matcher(json);
        assertTrue(matcher.find());
        return Long.parseLong(matcher.group(1));
    }
}
