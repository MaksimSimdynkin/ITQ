package ru.myproject.itq.dto;

import ru.myproject.itq.enums.DocumentHistoryAction;

import java.time.Instant;

public record DocumentHistoryDto(
        Long documentHistoryId,
        DocumentHistoryAction action,
        String actor,
        String comment,
        Instant createdAt
) {
}
