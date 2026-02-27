package ru.myproject.itq.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.myproject.itq.dto.BatchGetDocumentsRequest;
import ru.myproject.itq.dto.CreateDocumentRequest;
import ru.myproject.itq.dto.DocumentDto;
import ru.myproject.itq.dto.DocumentDetailsDto;
import ru.myproject.itq.enums.DocumentStatus;
import ru.myproject.itq.service.DocumentService;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDto> create(@Valid @RequestBody CreateDocumentRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.create(request));
    }

    @GetMapping("/{id}")
    public DocumentDetailsDto getOneWithHistory(@PathVariable Long id){
        return documentService.getOneHistoryDocument(id);
    }

    @PostMapping("/batch-get")
    public Page<DocumentDto> batchGet(@Valid @RequestBody BatchGetDocumentsRequest batchRequest, Pageable pageable){
        return documentService.batchGet(batchRequest.ids(), pageable);
    }

    @GetMapping("/search")
    public Page<DocumentDto> search(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            Pageable pageable
    ){
        return documentService.search(author, status, dateFrom, dateTo, pageable);
    }
}
