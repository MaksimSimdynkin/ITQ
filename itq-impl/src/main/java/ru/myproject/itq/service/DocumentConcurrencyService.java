package ru.myproject.itq.service;

import ru.myproject.itq.dto.ConcurrentApproveRequest;
import ru.myproject.itq.dto.ConcurrentApproveResponse;

public interface DocumentConcurrencyService {
    ConcurrentApproveResponse concurrentApprove(Long documentId, ConcurrentApproveRequest request);
}
