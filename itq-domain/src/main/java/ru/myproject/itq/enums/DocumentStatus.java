package ru.myproject.itq.enums;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentStatus {
    DRAFT("Создан"),
    SUBMITTED("Представленный"),
    APPROVED("Одобренный");

    private final String description;
}
