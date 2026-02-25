package ru.myproject.itq.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchGetDocumentsRequest(
        @NotEmpty
        @Size(max = 1000)
        List<Long> ids
) {
}
