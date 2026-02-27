package ru.myproject.itq.dto;

import ru.myproject.itq.enums.DocumentStatus;

public record ConcurrentApproveResponse(
        Long documentId,
        int threads,
        int attempts,
        int success,
        int conflict,
        int notFound,
        int registryError,
        int error,
        DocumentStatus finalStatus,
        long registryRecordsCount
) {
}
