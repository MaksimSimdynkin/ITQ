package ru.myproject.itq.service.impl;


import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.myproject.itq.entity.ApprovalRegistry;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.entity.DocumentHistory;
import ru.myproject.itq.enums.BatchItemResult;
import ru.myproject.itq.enums.DocumentHistoryAction;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.exeption.RegistryWriteFailedRuntimeException;
import ru.myproject.itq.repository.ApprovalRegistryRepository;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.repository.HistoryRepository;

@Service
@RequiredArgsConstructor
public class DocumentWorkService {

    private final DocumentRepository documentRepository;
    private final HistoryRepository historyRepository;
    private final ApprovalRegistryRepository approvalRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchItemResult submitOne(Long id, String initiator, String comment) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return BatchItemResult.NOT_FOUND;
        if (doc.getStatus() != DocumentStatus.DRAFT){
            return BatchItemResult.CONFLICT;
        }

        doc.setStatus(DocumentStatus.SUBMITTED);
        documentRepository.save(doc);

        historyRepository.save(DocumentHistory.builder()
                .documentId(doc)
                .action(DocumentHistoryAction.SUBMIT)
                .actor(initiator)
                .comment(comment)
                .build());
        return BatchItemResult.SUCCESS;

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchItemResult approveOne(Long id, String initiator, String comment) {
        Document doc = documentRepository.findById(id).orElse(null);
        if (doc == null) return BatchItemResult.NOT_FOUND;
        if (doc.getStatus() != DocumentStatus.SUBMITTED){
            return BatchItemResult.CONFLICT;
        }

        doc.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(doc);

        historyRepository.save(DocumentHistory.builder()
                .documentId(doc)
                .action(DocumentHistoryAction.APPROVE)
                .actor(initiator)
                .comment(comment)
                .build());
        try {
            approvalRepository.save(ApprovalRegistry.builder()
                    .documentId(doc)
                    .documentNumber(doc.getNumber())
                    .approvedBy(initiator)
                    .build());
        }catch (DataIntegrityViolationException exception){
            throw new RegistryWriteFailedRuntimeException("Ошибка регистрации = " + id, exception);
        }
        return BatchItemResult.SUCCESS;
    }
}
