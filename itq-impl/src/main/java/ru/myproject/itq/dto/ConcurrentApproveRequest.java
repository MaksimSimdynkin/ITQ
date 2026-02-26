package ru.myproject.itq.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConcurrentApproveRequest(
        @Min(value = 1, message = "threads должен быть >= 1")
        @Max(value = 200, message = "threads слишком большой (макс 200)")
        int threads,

        @Min(value = 1, message = "attempts должен быть >= 1")
        @Max(value = 20000, message = "attempts слишком большой (макс 20000)")
        int attempts,

        @NotBlank(message = "initiator не должен быть пустым")
        String initiator,

        String comment
) {
}
