package ru.myproject.itq.dto;

import lombok.Builder;

@Builder
public record ErrorDto(
        String code,
        String massage
) {
}
