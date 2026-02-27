package ru.myproject.itq.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentHistoryAction {
    SUBMIT("Представленный"),
    APPROVE("Одобренный");

    private final String description;
}
