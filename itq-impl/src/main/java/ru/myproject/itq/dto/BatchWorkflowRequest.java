package ru.myproject.itq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchWorkflowRequest(
        @NotEmpty(message = "ids не должен быть пустым")
        @Size(min = 1, max = 1000, message = "ids должен содержать от 1 до 1000 элементов")
        List<Long> ids,
        @NotBlank(message = "Поле initiator не должно быть пустым")
        String initiator,

        String comment
) {
}
