package ru.myproject.itq.dto;

import ru.myproject.itq.enums.DocumentStatus;

import java.time.Instant;


public record DocumentDto(
        Long documentId,
        Long number,
        String author,
        String title,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
