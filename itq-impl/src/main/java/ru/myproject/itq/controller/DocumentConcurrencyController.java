package ru.myproject.itq.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.myproject.itq.dto.ConcurrentApproveRequest;
import ru.myproject.itq.dto.ConcurrentApproveResponse;
import ru.myproject.itq.service.DocumentConcurrencyService;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Validated
public class DocumentConcurrencyController {

    private final DocumentConcurrencyService concurrencyService;

    @PostMapping("/{id}/concurrent")
    public ConcurrentApproveResponse concurrentApprove(
            @PathVariable Long id,
            @Valid @RequestBody ConcurrentApproveRequest request){
        return concurrencyService.concurrentApprove(id, request);
    }
}
