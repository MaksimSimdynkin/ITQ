package ru.myproject.itq.dto;

import ru.myproject.itq.enums.BatchItemResult;

public record BatchItemResponse(
        Long id,
        BatchItemResult result
) {
}
