package ru.myproject.itq.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.myproject.itq.dto.BatchWorkflowResponse;
import ru.myproject.itq.dto.BatchWorkflowRequest;
import ru.myproject.itq.service.DocumentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/documents")
@Validated
public class DocumentWorkflowController {

    private final DocumentService documentService;

    @PostMapping("/submit")
    public BatchWorkflowResponse submit(@Valid @RequestBody BatchWorkflowRequest request){
        return documentService.submit(request);
    }

    @PostMapping("/approve")
    public BatchWorkflowResponse approve(@Valid @RequestBody BatchWorkflowRequest request) {
        return documentService.approve(request);
    }
}
