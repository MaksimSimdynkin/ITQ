package ru.myproject.itq.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.myproject.itq.config.DocumentSpecifications;
import ru.myproject.itq.dto.BatchItemResponse;
import ru.myproject.itq.dto.BatchWorkflowResponse;
import ru.myproject.itq.dto.CreateDocumentRequest;
import ru.myproject.itq.dto.DocumentDetailsDto;
import ru.myproject.itq.dto.DocumentDto;
import ru.myproject.itq.dto.DocumentHistoryDto;
import ru.myproject.itq.dto.BatchWorkflowRequest;
import ru.myproject.itq.entity.Document;
import ru.myproject.itq.entity.DocumentHistory;
import ru.myproject.itq.enums.BatchItemResult;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.exeption.NotFoundException;
import ru.myproject.itq.exeption.RegistryWriteFailedRuntimeException;
import ru.myproject.itq.repository.DocumentRepository;
import ru.myproject.itq.service.DocumentService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final EntityManager entityManager;
    private final DocumentWorkService documentWorkService;

    @Override
    @Transactional
    public DocumentDto create(CreateDocumentRequest request) {
        log.info("Create document requested by initiator='{}', author='{}'", request.initiator(), request.author());
        Document document = Document.builder()
                .author(request.author())
                .title(request.title())
                .status(DocumentStatus.DRAFT)
                .build();
        Document saved = documentRepository.saveAndFlush(document);
        entityManager.refresh(saved);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDetailsDto getOneHistoryDocument(Long id) {
        Document doc = documentRepository.findWithHistoriesByDocumentId(id)
                .orElseThrow(() -> new NotFoundException("Документ не найден: " + id));

        List<DocumentHistoryDto> history = doc.getDocumentHistories().stream()
                .map(this::toHistoryDto)
                .toList();

        return new DocumentDetailsDto(
                doc.getDocumentId(),
                doc.getNumber(),
                doc.getAuthor(),
                doc.getTitle(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                history
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> batchGet(List<Long> ids, Pageable pageable) {
        return documentRepository.findByDocumentIdIn(ids, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDto> search(String author, DocumentStatus status, Instant dateFrom, Instant dateTo, Pageable pageable) {
        Specification<Document> doc = Specification
                .where(DocumentSpecifications.hasAuthor(author))
                .and(DocumentSpecifications.hasStatus(status))
                .and(DocumentSpecifications.createdFrom(dateFrom))
                .and(DocumentSpecifications.createdTo(dateTo));

        return documentRepository.findAll(doc, pageable).map(this::toDto);
    }

    @Override
    public BatchWorkflowResponse submit(BatchWorkflowRequest request) {
        List<BatchItemResponse> result = new ArrayList<>(request.ids().size());

        for (Long id : request.ids()){
            try {
                BatchItemResult res = documentWorkService.submitOne(id, request.initiator(), request.comment());
                result.add(new BatchItemResponse(id, res));
            }catch (ObjectOptimisticLockingFailureException e){
                result.add(new BatchItemResponse(id, BatchItemResult.CONFLICT));
            } catch (Exception e) {
                result.add(new BatchItemResponse(id, BatchItemResult.ERROR));
            }
        }
        return new BatchWorkflowResponse(result);
    }



    @Override
    public BatchWorkflowResponse approve(BatchWorkflowRequest request) {
        List<BatchItemResponse> result = new ArrayList<>(request.ids().size());

        for (Long id : request.ids()){
            try {
                BatchItemResult res = documentWorkService.approveOne(id, request.initiator(), request.comment());
                result.add(new BatchItemResponse(id, res));
            } catch (RegistryWriteFailedRuntimeException e) {
                result.add(new BatchItemResponse(id, BatchItemResult.REGISTRY_ERROR));
            }catch (ObjectOptimisticLockingFailureException ex){
                result.add(new BatchItemResponse(id, BatchItemResult.CONFLICT));
            } catch (Exception e) {
                result.add(new BatchItemResponse(id, BatchItemResult.ERROR));
            }
        }
        return new BatchWorkflowResponse(result);
    }

    private DocumentDto toDto(Document d) {
        return new DocumentDto(
                d.getDocumentId(),
                d.getNumber(),
                d.getAuthor(),
                d.getTitle(),
                d.getStatus(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
    private DocumentHistoryDto toHistoryDto(DocumentHistory h) {
        return new DocumentHistoryDto(
                h.getDocumentHistoryId(),
                h.getAction(),
                h.getActor(),
                h.getComment(),
                h.getCreatedAt()
        );
    }
}
