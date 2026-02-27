package ru.myproject.itq.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.myproject.itq.dto.BatchWorkflowResponse;
import ru.myproject.itq.dto.CreateDocumentRequest;
import ru.myproject.itq.dto.DocumentDetailsDto;
import ru.myproject.itq.dto.DocumentDto;
import ru.myproject.itq.dto.BatchWorkflowRequest;
import ru.myproject.itq.enums.DocumentStatus;

import java.time.Instant;
import java.util.List;


public interface DocumentService {

    DocumentDto create(CreateDocumentRequest request);

    DocumentDetailsDto getOneHistoryDocument(Long id);

    Page<DocumentDto> batchGet(List<Long> ids, Pageable pageable);

    Page<DocumentDto> search(String author, DocumentStatus status, Instant dateFrom, Instant dateTo, Pageable pageable);

    BatchWorkflowResponse submit(BatchWorkflowRequest request);

    BatchWorkflowResponse approve(BatchWorkflowRequest request);
}


