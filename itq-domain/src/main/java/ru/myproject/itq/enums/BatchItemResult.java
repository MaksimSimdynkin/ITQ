package ru.myproject.itq.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BatchItemResult {
    SUCCESS("успешно"),
    NOT_FOUND("не найдено"),
    CONFLICT("конфликт"),
    REGISTRY_ERROR("ошибка регистрации в реестре."),
    ERROR("ошибка");

    private final String description;
}
