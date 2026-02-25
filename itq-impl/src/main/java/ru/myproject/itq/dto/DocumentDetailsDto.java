package ru.myproject.itq.dto;

import ru.myproject.itq.enums.DocumentStatus;

import java.time.Instant;
import java.util.List;

public record DocumentDetailsDto(
        Long documentId,
        Long number,
        String author,
        String title,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<DocumentHistoryDto> history
) {}