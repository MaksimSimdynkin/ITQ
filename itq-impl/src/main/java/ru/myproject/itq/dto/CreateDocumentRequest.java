package ru.myproject.itq.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @NotBlank(message = "Поле author не должно быть пустым")
        String author,
        @NotBlank(message = "Поле title не должно быть пустым")
        String title,
        @NotBlank(message = "Поле initiator не должно быть пустым")
        String initiator
) {
}
